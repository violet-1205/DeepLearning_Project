# 🧠 Spring Boot & Python Deep Learning Integration Project

## 📖 프로젝트 소개 (Project Overview)
이 프로젝트는 **Java Spring Boot** 웹 애플리케이션과 **Python 딥러닝** 모델을 연동하여 구축한 웹 서비스입니다.  
사용자가 웹 인터페이스를 통해 데이터를 입력하면, Spring Boot 백엔드가 Python 스크립트를 실행하여 딥러닝 모델(TensorFlow/Keras)을 통한 예측 결과를 반환합니다.

웹 개발의 안정성과 딥러닝의 강력한 기능을 결합한 **Polyglot Programming** 구조를 학습하고 구현하는 것을 목표로 합니다.

---

## 🚀 주요 기능 (Key Features)

### 1. 🏥 당뇨병 발병 예측 (Diabetes Prediction)
- **데이터셋**: Pima Indians Diabetes Dataset
- **기능**: 임신 횟수, 혈당, 혈압 등 8가지 건강 지표를 입력받아 당뇨병 발병 확률을 예측합니다.
- **기술**: Keras Sequential 모델, 이진 분류(Binary Classification).

### 2. 🌸 붓꽃 품종 분류 (Iris Classification)
- **데이터셋**: Iris Dataset
- **기능**: 꽃받침(Sepal)과 꽃잎(Petal)의 길이/너비를 기반으로 붓꽃의 품종(Setosa, Versicolor, Virginica)을 분류합니다.
- **기술**: 다중 분류(Multi-class Classification), One-Hot Encoding.

### 3. 🩺 폐암 수술 후 생존 예측 (Lung Cancer Survival Prediction)
- **데이터셋**: Thoraric Surgery Dataset
- **기능**: 환자의 진단명, 폐활량, 흡연 여부 등 16가지 의료 데이터를 분석하여 수술 후 생존 가능성을 예측합니다.
- **기술**: Keras Sequential 모델, 이진 분류(Binary Classification).

## �🛠 기술 스택 (Tech Stack)

### 🖥️ Backend & Frontend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle

### 🧠 AI & Data Science
- **Language**: Python 3.10
- **Libraries**: 
  - TensorFlow / Keras (Deep Learning)
  - Scikit-learn (Preprocessing)
  - Pandas, NumPy (Data Manipulation)

---

## 📂 프로젝트 구조 (Project Structure)

```
springboot/
├── src/main/java/com/python/deep/   # Java Controller (Spring Boot)
│   └── controller/DeepLearningController.java  # Python 실행 및 결과 처리
├── src/main/resources/
│   ├── static/
│   │   ├── python/                  # Python 딥러닝 스크립트 & 모델 파일
│   │   │   ├── diabetes_deep.py     # 당뇨병 예측 스크립트
│   │   │   ├── iris_deep.py         # 붓꽃 분류 스크립트
│   │   │   ├── lung_deep.py         # 폐암 생존 예측 스크립트
│   │   │   └── *.h5, *.pkl          # 학습된 모델 및 스케일러
│   │   └── data/                    # 데이터셋 (CSV)
│   └── templates/                   # HTML 화면 (Thymeleaf)
│       └── deep/
│           ├── diabetes.html        # 당뇨병 예측 페이지
│           ├── iris.html            # 붓꽃 분류 페이지
│           └── lung.html            # 폐암 예측 페이지
└── build.gradle                     # 프로젝트 의존성 관리
```

---

## ⚙️ 실행 흐름 (How it Works)

1. **User**: 웹 브라우저에서 데이터 입력 및 '분석하기' 클릭.
2. **Spring Boot**: `ProcessBuilder`를 사용하여 Python 인터프리터 실행.
3. **Python**: 
   - 학습된 모델(`.h5`) 로드.
   - 입력 데이터 전처리 (Scaling/Encoding).
   - 예측 수행 (`model.predict`).
   - 결과를 표준 출력(Standard Output)으로 반환.
4. **Spring Boot**: Python의 출력값을 캡처하여 HTML 템플릿에 맵핑.
5. **Browser**: 사용자에게 분석 결과와 확률 표시.

---

## 📝 설치 및 실행 (Installation & Run)

1. **Python 환경 설정**
   - Python 3.10 설치.
   - 필수 라이브러리 설치:
     ```bash
     pip install tensorflow pandas numpy scikit-learn
     ```

2. **Spring Boot 실행**
   - 프로젝트 루트에서 실행:
     ```bash
     ./gradlew bootRun
     ```
   - 접속: `http://localhost:1111`
