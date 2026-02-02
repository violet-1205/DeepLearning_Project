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

import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from tensorflow.keras.models import Sequential, load_model
from tensorflow.keras.layers import Dense
from tensorflow.keras.utils import to_categorical

# 재현성을 위한 시드 고정
np.random.seed(42)
tf.random.set_seed(42)

# ==================== 1. 데이터 준비 ====================
# 현재 스크립트의 경로를 기준으로 데이터 경로 설정
# Use relative path assuming CWD is set correctly by Java
BASE_DIR = "."
# static/python에서 상위(static) -> data 폴더로 이동
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "iris3.csv")

# CSV 파일에서 데이터 불러오기
try:
    df = pd.read_csv(DATA_PATH, encoding="utf-8")
except FileNotFoundError:
    # Fallback to relative path if absolute fails due to encoding
    DATA_PATH = "../data/iris3.csv"
    try:
        df = pd.read_csv(DATA_PATH, encoding="utf-8")
    except:
        print(f"Error: Data file not found at {DATA_PATH}")
        sys.exit(1)

# X: 속성 (꽃받침 길이/너비, 꽃잎 길이/너비)
# y: 품종 (정답)
X = df.iloc[:, :-1]
y = df.iloc[:, -1]

# 모델 파일 경로 (현재 스크립트와 같은 위치)
MODEL_PATH = os.path.join(BASE_DIR, "iris_model.h5")
SCALER_PATH = os.path.join(BASE_DIR, "iris_scaler.pkl")
ENCODER_PATH = os.path.join(BASE_DIR, "iris_encoder.pkl")

# 모델 로드 또는 학습
if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH) and os.path.exists(ENCODER_PATH):
    model = load_model(MODEL_PATH)
    scaler = joblib.load(SCALER_PATH)
    encoder = joblib.load(ENCODER_PATH)
else:
    # 데이터 전처리
    encoder = LabelEncoder()
    y_encoded = encoder.fit_transform(y)
    y_onehot = to_categorical(y_encoded)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y_onehot, test_size=0.2, random_state=42, stratify=y_encoded
    )

    scaler = StandardScaler()
    X_train_sc = scaler.fit_transform(X_train)
    X_test_sc = scaler.transform(X_test)

    # 모델 구성
    model = Sequential([
        Dense(16, input_shape=(4,), activation='relu'),
        Dense(12, activation='relu'),
        Dense(3, activation='softmax')
    ])

    model.compile(
        loss='categorical_crossentropy',
        optimizer='adam',
        metrics=['accuracy']
    )

    # 모델 학습
    model.fit(X_train_sc, y_train, epochs=100, batch_size=5, verbose=0)
    
    # 모델 저장
    model.save(MODEL_PATH)
    joblib.dump(scaler, SCALER_PATH)
    joblib.dump(encoder, ENCODER_PATH)

# ==================== 6. 사용자 입력 예측 ====================
try:
    if len(sys.argv) > 1:
        sepal_length = float(sys.argv[1])
        sepal_width = float(sys.argv[2])
        petal_length = float(sys.argv[3])
        petal_width = float(sys.argv[4])
    else:
        # 기본값 (테스트용)
        sepal_length = 5.1
        sepal_width = 3.5
        petal_length = 1.4
        petal_width = 0.2

    input_data = np.array([[sepal_length, sepal_width, petal_length, petal_width]])
    
    # 스케일링
    input_scaled = scaler.transform(input_data)
    
    # 예측
    pred_proba = model.predict(input_scaled, verbose=0)
    pred_index = np.argmax(pred_proba)
    pred_class = encoder.inverse_transform([pred_index])[0]
    confidence = pred_proba[0][pred_index] * 100
    
    print(f"예측 결과: {pred_class}")
    print(f"확률: {confidence:.2f}%")

except ValueError:
    print("잘못된 입력입니다. 숫자를 입력해주세요.")
except Exception as e:
    print(f"오류 발생: {e}")
