import pandas as pd
import numpy as np
import sys
import os
import io
import joblib

# Set stdout/stderr encoding to UTF-8 for Java ProcessBuilder compatibility
sys.stdout = io.TextIOWrapper(sys.stdout.detach(), encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.detach(), encoding='utf-8')

# Suppress TensorFlow logs
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import tensorflow as tf
from tensorflow.keras.models import Sequential, load_model
from tensorflow.keras.layers import Dense, Dropout, BatchNormalization
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau
from tensorflow.keras.optimizers import Adam

# 재현성을 위한 시드 고정
np.random.seed(42)
tf.random.set_seed(42)

# ==================== 1. 데이터 준비 ====================
# 현재 스크립트의 경로를 기준으로 데이터 경로 설정
# Use relative path assuming CWD is set correctly by Java
BASE_DIR = "."
# static/python에서 상위(static) -> data 폴더로 이동
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "ThoraricSurgery3.csv")
MODEL_PATH = "lung_model.h5"
SCALER_PATH = "lung_scaler.pkl"

# CSV 파일에서 폐암 수술 환자 데이터 불러오기
try:
    df = pd.read_csv(DATA_PATH, header=None)
except FileNotFoundError:
    # Fallback to relative path if absolute fails due to encoding
    DATA_PATH = "../data/ThoraricSurgery3.csv"
    try:
        df = pd.read_csv(DATA_PATH, header=None)
    except:
        print(f"Error: Data file not found at {DATA_PATH}")
        sys.exit(1)

# X: 입력 데이터 (마지막 열 제외한 모든 열)
# y: 정답 레이블 (마지막 열: 수술 후 사망 여부)
X = df.iloc[:, :-1]
y = df.iloc[:, -1]

# 모델 로드 또는 학습
if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
    # print("저장된 모델을 불러옵니다...")
    model = load_model(MODEL_PATH)
    scaler = joblib.load(SCALER_PATH)
else:
    # print("모델을 새로 학습합니다...")
    
    # 데이터 분할
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, 
        test_size=0.2,      # 테스트 데이터 20%
        random_state=42,    # 재현 가능성 확보
        stratify=y          # 클래스 비율 유지
    )

    # 스케일링
    scaler = StandardScaler()
    X_train_sc = scaler.fit_transform(X_train)
    X_test_sc = scaler.transform(X_test)

    # 모델 구성
    model = Sequential([
        Dense(64, activation="relu", input_shape=(X_train_sc.shape[1],)),
        BatchNormalization(),
        Dropout(0.3),
        Dense(32, activation="relu"),
        BatchNormalization(),
        Dropout(0.2),
        Dense(16, activation="relu"),
        Dense(1, activation="sigmoid")
    ])

    model.compile(
        optimizer=Adam(learning_rate=0.001),
        loss="binary_crossentropy",
        metrics=["accuracy"]
    )

    early_stop = EarlyStopping(monitor="val_loss", patience=20, restore_best_weights=True, verbose=1)
    reduce_lr = ReduceLROnPlateau(monitor="val_loss", factor=0.5, patience=10, min_lr=0.00001, verbose=1)

    model.fit(
        X_train_sc, y_train,
        validation_split=0.2,
        epochs=200,
        batch_size=16,
        callbacks=[early_stop, reduce_lr],
        verbose=0 # Suppress training log for web execution
    )

    # 모델 및 스케일러 저장
    model.save(MODEL_PATH)
    joblib.dump(scaler, SCALER_PATH)
    # print("모델 저장 완료.")

# ==================== 사용자 입력 예측 ====================
try:
    if len(sys.argv) > 1:
        # 커맨드 라인 인자가 있는 경우
        # 입력된 인자들을 float로 변환 (첫 번째 인자는 스크립트 이름이므로 제외)
        input_values = [float(x) for x in sys.argv[1:]]
        
        if len(input_values) != X.shape[1]:
             print(f"Error: Expected {X.shape[1]} features, but got {len(input_values)}")
             sys.exit(1)

        # 입력 데이터 DataFrame 생성
        input_data = pd.DataFrame([input_values], columns=X.columns)

        # 스케일링 적용
        input_scaled = scaler.transform(input_data)

        # 예측
        pred_proba = model.predict(input_scaled, verbose=0)
        pred_class = 1 if pred_proba[0][0] > 0.5 else 0

        print(f"예측 결과: {'폐암 수술 후 사망 위험 높음' if pred_class == 1 else '생존 가능성 높음'}")
        print(f"확률: {pred_proba[0][0]*100:.2f}%")

    else:
        # 인자가 없으면 데이터 정보 출력 등
        pass

except Exception as e:
    print(f"Error: {e}")
