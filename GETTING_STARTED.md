# Getting Started - Sun Direction App 실행 가이드

GitHub에서 다운로드한 파일들 중 어떤 파일을 어떻게 실행해야 하는지 단계별로 설명합니다.

## 📋 목차
1. [사전 요구사항](#사전-요구사항)
2. [데스크톱 앱 실행](#데스크톱-앱-실행방법)
3. [모바일 웹앱 실행](#모바일-웹앱-실행방법)
4. [빌드 및 패키징](#빌드-및-패키징)

---

## 사전 요구사항

### 데스크톱 앱 실행만 하고 싶으면
- Windows OS
- 추가 설치 불필요 (**.exe 파일 직접 실행 가능**)

### 데스크톱 앱을 직접 빌드하고 싶으면
- Java 21 이상 설치
- .NET 9.0 이상 설치
- PowerShell 5.1 이상 (Windows에 기본 포함)

### 모바일 웹앱을 실행하고 싶으면
- Python 3.7 이상 설치
- 최신 웹 브라우저 (Chrome, Firefox, Safari, Edge 등)

---

## 데스크톱 앱 실행방법

### 방법 1️⃣: 설치 프로그램으로 설치 (가장 간단) ⭐ **권장**

이미 빌드된 설치 프로그램을 사용합니다.

**경로**: `dist/SunDirectionApp-Installer.exe`

**실행 방법**:
```
dist/ 폴더에서 SunDirectionApp-Installer.exe 파일을 더블클릭
```

**설치 과정**:
1. 설치 프로그램이 실행됨
2. 자동으로 프로그램이 `C:\Users\[사용자명]\AppData\Local\SunDirectionApp\` 폴더에 설치됨
3. 데스크톱과 시작메뉴에 바로가기가 생성됨
4. 데스크톱의 바로가기 또는 시작메뉴에서 "Sun Direction App" 실행

---

### 방법 2️⃣: 포터블 버전 (설치 없이 직접 실행)

설치 없이 압축 파일에서 직접 실행할 수 있습니다.

**경로**: `dist/SunDirectionApp.zip`

**실행 방법**:
1. `SunDirectionApp.zip` 파일을 압축 해제
2. 압축 해제된 폴더에서 `SunDirectionApp.exe` 찾기
3. `SunDirectionApp.exe` 더블클릭으로 실행

**장점**: 설치 불필요, 어디서나 실행 가능

---

### 방법 3️⃣: 소스에서 직접 빌드

Java 21과 .NET 9.0을 설치했다면 직접 빌드할 수 있습니다.

**Step 1: PowerShell 열기**
```powershell
# 프로젝트 폴더에서 PowerShell 열기 (Shift + 우클릭)
# 또는 명령어: cd C:\경로\to\프로젝트
```

**Step 2: 빌드 스크립트 실행**
```powershell
.\package-app.ps1
```

**출력 결과**:
- `dist/SunDirectionApp/SunDirectionApp.exe` ← 데스크톱 실행 파일
- `dist/SunDirectionApp.zip` ← 포터블 버전
- `dist/SunDirectionApp-Installer.exe` ← 설치 프로그램

**주의**: PowerShell 실행 정책 문제가 나면 이 명령어 입력:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser
```

---

## 모바일 웹앱 실행방법

### 준비물
- Python 3.7 이상 설치 확인

**Python 설치 확인**:
```powershell
python --version
```

---

### 스텝 1: 프로젝트 폴더에 PowerShell 열기

```powershell
cd C:\경로\to\프로젝트
```

---

### 스텝 2: 웹 서버 시작

```powershell
.\start-mobile-web.ps1
```

**출력 결과**:
```
Serving HTTP on :: port 8080 (http://[::]:8080/) ...
```

---

### 스텝 3: 브라우저에서 접속

아래 주소를 브라우저에 입력:

```
http://localhost:8080/mobile-web/
```

또는 

```
http://127.0.0.1:8080/mobile-web/
```

---

### 모바일 기기에서 접속 (같은 WiFi 네트워크)

**Windows PC의 IP 주소 찾기**:
```powershell
ipconfig
```

출력에서 `IPv4 Address` 찾기 (예: `192.168.0.100`)

**모바일 기기의 브라우저에 입력**:
```
http://192.168.0.100:8080/mobile-web/
```

---

## 웹앱 기능 설명

### 🔍 도시 검색
1. 검색창에 도시명 입력 (영문 또는 한글)
2. Enter 키 또는 검색 버튼 클릭
3. 자동으로 좌표가 설정되고 태양 방향 계산

**예**: `Seoul`, `Tokyo`, `New York`

### 📍 GPS 사용
1. "현재 위치" 버튼 클릭
2. GPS 권한 허용 선택
3. 자동으로 위치가 감지되고 태양 방향 계산

### 🧭 나침반 보기
- 중앙의 원형 나침반에 태양 방향 표시
- 녹색: 현재 태양 위치
- 파란색: 북쪽 (0°)
- 빨간색: 남쪽 (180°)

### ☀️ / 🌙 주야 표시
- **낮** (태양이 수평선 위): 밝은 배경
- **밤** (태양이 수평선 아래): 어두운 배경

### 📱 PWA 설치 (모바일)
- iOS: "홈 화면에 추가" 버튼으로 설치
- Android: "설치" 버튼으로 설치 후 마치 앱처럼 사용 가능

---

## 웹 서버 중지

**PowerShell 창에서 Ctrl + C 누르기**

```
^C^C
```

또는 PowerShell 창 닫기

---

## 📊 파일 구조 한눈에 보기

```
📁 프로젝트 폴더
│
├─ 🔴 **실행 파일** (이 파일들을 실행하세요!)
│  ├─ dist/SunDirectionApp-Installer.exe  ← 데스크톱 앱 설치 (추천)
│  ├─ dist/SunDirectionApp.zip            ← 포터블 버전
│  └─ start-mobile-web.ps1                ← 모바일 웹앱 시작
│
├─ 📝 **설정 파일** (수정 불필요)
│  ├─ package-app.ps1         ← 빌드 자동화
│  ├─ build-dotnet-installer.ps1
│  ├─ generate-assets.ps1
│  └─ README.md               ← 프로젝트 설명
│
├─ 📂 **소스 코드** (개발자용)
│  ├─ src/               ← Java 소스코드
│  ├─ mobile-web/        ← 웹앱 소스코드
│  └─ installer/         ← 설치 프로그램 소스코드
│
└─ 🎨 **리소스**
   └─ assets/            ← 아이콘, 다이어그램
```

---

## ❓ 자주 묻는 질문 (FAQ)

### Q1: 설치 프로그램 실행이 안 돼요
**A**: 다음을 확인하세요:
- Windows Defender 또는 백신이 파일을 차단했는지 확인
- 우클릭 → "관리자 권한으로 실행" 시도
- `dist/` 폴더가 실제로 존재하는지 확인

### Q2: 데스크톱 앱이 실행 안 됨
**A**: 
- .NET 9.0이 설치되어 있는지 확인: `dotnet --info`
- Windows 스토어에서 "Desktop Runtime" 설치

### Q3: 웹앱이 접속이 안 됨
**A**:
- PowerShell 창이 닫혔는지 확인 (서버가 중지됨)
- `python --version` 확인
- 다시 `.\start-mobile-web.ps1` 실행
- 방화벽이 8080 포트를 차단했는지 확인

### Q4: 모바일에서 GPS 권한이 묻지 않음
**A**:
- HTTPS가 필요: 프로덕션에서는 HTTPS 호스팅 필요
- 로컬호스트는 HTTP도 지원하지만, localhost:8080만 가능

### Q5: 오프라인에서도 사용 가능한가?
**A**:
- Web앱: 캐시된 페이지는 오프라인에서 사용 가능
- 도시 검색: 인터넷 필요
- GPS 위치 + 계산: 오프라인 가능

### Q6: 계산 결과가 정확한가?
**A**:
- NOAA 알고리즘 사용 (정확도: ±0.2°)
- 일출/일몰 시간은 정확함
- 대기 굴절은 고려하지 않음

---

## 🚀 다음 단계

1. **데스크톱 앱 실행**: `dist/SunDirectionApp-Installer.exe` 더블클릭
2. **모바일 웹앱 체험**: `.\start-mobile-web.ps1` 실행 후 `http://localhost:8080/mobile-web/` 접속
3. **소스 코드 수정**: `src/` 또는 `mobile-web/` 폴더의 파일 편집 후 재빌드

---

**문제가 있으면 각 섹션의 FAQ를 참고하세요!** 😊
