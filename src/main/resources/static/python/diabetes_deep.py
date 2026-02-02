import pandas as pd
import numpy as np
import sys
import os
import io

# Set stdout/stderr encoding to UTF-8 for Java ProcessBuilder compatibility
sys.stdout = io.TextIOWrapper(sys.stdout.detach(), encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.detach(), encoding='utf-8')

import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.impute import SimpleImputer

# Suppress TensorFlow logs
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

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
# BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# Use relative path assuming CWD is set correctly by Java
BASE_DIR = "."
# static/python에서 상위(static) -> data 폴더로 이동
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "pima-indians-diabetes3.csv")

# CSV 파일에서 당뇨병 데이터 불러오기
try:
    df = pd.read_csv(DATA_PATH, encoding="utf-8")
except FileNotFoundError:
    # 절대 경로 fallback (개발 환경용) - 경로 수정
    # DATA_PATH = r"C:\Users\320-16\Desktop\파이썬\springboot\src\main\resources\static\data\pima-indians-diabetes3.csv"
    # Fallback to relative path if absolute fails due to encoding
    DATA_PATH = "../data/pima-indians-diabetes3.csv"
    try:
        df = pd.read_csv(DATA_PATH, encoding="utf-8")
    except:
        print(f"Error: Data file not found at {DATA_PATH}")
        sys.exit(1)

# 0값을 결측치(NaN)로 변환
zero_features = ['plasma', 'pressure', 'thickness', 'insulin', 'bmi']
df[zero_features] = df[zero_features].replace(0, np.nan)

# 모델 파일 경로 (현재 스크립트와 같은 위치)
MODEL_PATH = os.path.join(BASE_DIR, "diabetes_model.h5")
SCALER_PATH = os.path.join(BASE_DIR, "diabetes_scaler.pkl")
IMPUTER_PATH = os.path.join(BASE_DIR, "diabetes_imputer.pkl")

# 모델 로드 또는 학습
if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH) and os.path.exists(IMPUTER_PATH):
    # print("저장된 모델을 불러옵니다...")
    model = load_model(MODEL_PATH)
    scaler = joblib.load(SCALER_PATH)
    imputer = joblib.load(IMPUTER_PATH)
    
    # X 컬럼 정보 필요
    X = df.iloc[:, :-1]
else:
    print("모델을 새로 학습합니다...")
    # 평균값으로 대체
    imputer = SimpleImputer(strategy='median')
    df[zero_features] = imputer.fit_transform(df[zero_features])

    # X, y 분리
    X = df.iloc[:, :-1]
    y = df.iloc[:, -1]

    # 데이터 분할
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=7, stratify=y
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
        batch_size=10,
        callbacks=[early_stop, reduce_lr],
        verbose=0
    )
    
    # 모델 저장
    model.save(MODEL_PATH)
    joblib.dump(scaler, SCALER_PATH)
    joblib.dump(imputer, IMPUTER_PATH)

# 사용자 입력 예측
try:
    if len(sys.argv) > 1:
        pregnant = float(sys.argv[1])
        plasma = float(sys.argv[2])
        pressure = float(sys.argv[3])
        thickness = float(sys.argv[4])
        insulin = float(sys.argv[5])
        bmi = float(sys.argv[6])
        pedigree = float(sys.argv[7])
        age = float(sys.argv[8])
    else:
        # 테스트용 기본값
        pregnant = 6
        plasma = 148
        pressure = 72
        thickness = 35
        insulin = 0
        bmi = 33.6
        pedigree = 0.627
        age = 50

    input_data = pd.DataFrame([[pregnant, plasma, pressure, thickness, insulin, bmi, pedigree, age]], columns=X.columns)
    
    # 0값 처리 (학습 때와 동일하게)
    input_data[zero_features] = input_data[zero_features].replace(0, np.nan)
    input_data[zero_features] = imputer.transform(input_data[zero_features])
    
    # 스케일링
    input_scaled = scaler.transform(input_data)
    
    # 예측
    pred_proba = model.predict(input_scaled, verbose=0)
    pred_class = 1 if pred_proba[0][0] > 0.5 else 0
    
    print(f"예측 결과: {'당뇨병 양성 (발병 가능성 높음)' if pred_class == 1 else '당뇨병 음성 (정상)'}")
    print(f"확률: {pred_proba[0][0]*100:.2f}%")

except ValueError:
    print("잘못된 입력입니다. 숫자를 입력해주세요.")
except Exception as e:
    print(f"오류 발생: {e}")