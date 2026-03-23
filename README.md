# Folder Organizer

선택한 폴더 안의 **파일만** 읽어서 확장자 기준으로 분류하고,  
원본 폴더 옆에 **`(폴더이름)_정리결과`** 폴더를 만들어 정리해주는 Windows용 데스크톱 앱입니다.

> 폴더는 이동하지 않으며, 항상 정리 대상에서 제외됩니다.  
> 정리 후에는 **로그 파일 저장**, **되돌리기**, **로그 파일 선택 되돌리기**를 지원합니다.

---

## Preview

- 폴더 선택
- 파일 목록 읽기
- 자동 분류
- 분류 수정
- 정리 실행
- 정리 결과 폴더 자동 열기
- 되돌리기
- 오류 상세 보기

---

## Features

- 파일 확장자 기준 자동 분류
- 파일만 정리, 폴더는 항상 제외
- 분류결과 / 이동 예정 폴더 직접 수정 가능
- 정리 제외 체크 가능
- 정리 실행 전 확인 팝업
- 원본 폴더 옆에 정리 결과 폴더 생성
- 작업 로그 저장
- 마지막 작업 되돌리기
- 로그 파일 선택 후 예전 작업 되돌리기
- 오류 상세 보기
- 빈 정리 폴더 자동 정리
- 실행 후 테이블 자동 새로고침

---

## How It Works

예를 들어 원본 폴더가 아래와 같으면:

```text
C:\Users\User\Desktop

정리 결과는 아래처럼 생성됩니다:

C:\Users\User\Desktop_정리결과

예시 구조:

Desktop_정리결과
 ┣ Images
 ┣ Documents
 ┣ Designs
 ┣ Videos
 ┣ Audio
 ┣ Archives
 ┣ Programs
 ┣ Shortcuts
 ┣ Others
 ┗ Desktop_20260321_130501.txt
File Classification Rules
Images
png
jpg
jpeg
gif
bmp
webp
svg
Designs
psd
psb
ai
xd
Documents
pdf
doc
docx
txt
hwp
hwpx
hwt
xls
xlsx
ppt
pptx
Videos
mp4
avi
mkv
mov
wmv
Audio
mp3
wav
flac
aac
Archives
zip
rar
7z
tar
gz
Programs
exe
msi
bat
cmd
Shortcuts
lnk
url
Others
위 목록에 없는 파일
Folders
항상 정리 제외
Tech Stack
Java 21
JavaFX
Gradle
jpackage
Development Environment
Windows
VS Code
JDK 21+

권장 VS Code 확장:

Extension Pack for Java
Gradle for Java
Project Structure
FolderOrganizer
 ┣ assets
 ┃ ┗ app-icon.ico
 ┣ src
 ┃ ┗ main
 ┃   ┣ java
 ┃   ┃ ┗ app
 ┃   ┃   ┣ FileClassifier.java
 ┃   ┃   ┣ FileItem.java
 ┃   ┃   ┣ FileScanner.java
 ┃   ┃   ┣ Launcher.java
 ┃   ┃   ┣ MainApp.java
 ┃   ┃   ┣ OrganizerService.java
 ┃   ┃   ┗ UndoService.java
 ┃   ┗ resources
 ┃     ┗ app
 ┃       ┣ icon.png
 ┃       ┗ styles.css
 ┣ build.gradle
 ┣ settings.gradle
 ┣ gradlew
 ┗ gradlew.bat
Run in Development
.\gradlew.bat run
Build
Standard Build
.\gradlew.bat clean build
Create Distribution Layout
.\gradlew.bat clean installDist
Packaging
Create app-image
jpackage --type app-image --name FolderOrganizer --app-version 1.0.0 --input build\install\FolderOrganizer\lib --main-jar FolderOrganizer-1.0.0.jar --main-class app.Launcher --icon assets\app-icon.ico --dest dist
Create MSI
jpackage --type msi --name FolderOrganizer --app-version 1.0.0 --input build\install\FolderOrganizer\lib --main-jar FolderOrganizer-1.0.0.jar --main-class app.Launcher --icon assets\app-icon.ico --dest dist --win-dir-chooser --win-menu --win-shortcut
Create EXE
jpackage --type exe --name FolderOrganizer --app-version 1.0.0 --input build\install\FolderOrganizer\lib --main-jar FolderOrganizer-1.0.0.jar --main-class app.Launcher --icon assets\app-icon.ico --dest dist --win-dir-chooser --win-menu --win-menu-group "Folder Organizer" --win-shortcut --win-shortcut-prompt
Pre-release Checklist
.\gradlew.bat run 정상 실행
폴더 선택 가능
목록 읽기 가능
정리 실행 가능
정리 폴더 자동 열기 가능
되돌리기 가능
로그 파일 선택 되돌리기 가능
아이콘 정상 표시
styles.css 정상 적용
설치본 실행 테스트 완료
Usage
앱 실행
정리할 폴더 선택
목록 읽기
필요 시 분류결과 / 이동 예정 폴더 수정
필요 시 정리 제외 체크
정리 실행
정리 결과 폴더 확인
필요 시 되돌리기 실행
Log File Format

정리 실행 시 로그 파일은 아래 형식으로 저장됩니다.

(폴더이름)_(시간).txt

예시:

Desktop_20260321_130501.txt

로그 내용 예시:

C:\Users\User\Desktop\cat.png|C:\Users\User\Desktop_정리결과\Images\cat.png
C:\Users\User\Desktop\resume.hwp|C:\Users\User\Desktop_정리결과\Documents\resume.hwp
Undo
Undo Last Operation

앱의 되돌리기 버튼 사용

Undo Previous Operation with Log File

앱의 로그 파일 선택 되돌리기 버튼 사용 후
저장된 .txt 또는 .log 파일 선택

Exception Handling

앱은 아래 상황에 대한 예외 처리를 포함합니다.

파일 없음
권한 없음
파일 시스템 오류
잘못된 로그 형식
폴더 자동 열기 실패
잠긴 파일
이미 이동된 파일
리소스 파일(css, icon) 누락 시 안전 처리

오류 발생 시 상태 메시지와 오류 상세 보기 창에서 확인할 수 있습니다.

Auto Cleanup

되돌리기 후 아래 항목이 자동 정리됩니다.

비어 있는 Images, Documents 등 하위 폴더
로그 파일만 남은 경우 로그 파일 삭제
상위 정리 폴더가 완전히 비면 상위 정리 폴더 삭제
Icons
App Window Icon
src/main/resources/app/icon.png
Packaging Icon
assets/app-icon.ico
build.gradle Example
plugins {
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.1.0'
}

group = 'app'
version = '1.0.0'

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

javafx {
    version = '21'
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}

application {
    mainClass = 'app.Launcher'
}
Why Launcher Is Used

JavaFX 앱을 jpackage로 배포할 때
Application 상속 클래스(MainApp)를 직접 메인 클래스로 지정하면 아래 오류가 발생할 수 있습니다.

Error: JavaFX runtime components are missing, and are required to run this application

이를 해결하기 위해 일반 클래스인 Launcher를 만들고,
그 안에서 MainApp.main(args)를 호출하는 구조를 사용합니다.

Launcher.java
package app;

public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
Troubleshooting
1. JavaFX runtime components are missing

원인

MainApp을 직접 메인 클래스로 패키징했을 가능성

해결

Launcher.java 생성
build.gradle의 mainClass를 app.Launcher로 변경
jpackage의 --main-class도 app.Launcher로 변경
2. candle.exe / light.exe not found

원인

Windows exe, msi 패키징 도구(WiX Toolset) 미설치 또는 PATH 미설정

해결

WiX Toolset v3 설치
WiX bin 경로를 PATH에 추가
새 PowerShell에서 아래 확인
where.exe candle.exe
where.exe light.exe
3. Cannot delete dist folder

원인

실행 중인 FolderOrganizer.exe
java.exe, javaw.exe
설치 프로그램 프로세스가 dist 폴더를 점유

해결

taskkill /F /IM FolderOrganizer.exe
taskkill /F /IM java.exe
taskkill /F /IM javaw.exe
rmdir /S /Q .\dist
4. Nothing happens when launching shortcut

확인

app-image 실행 여부 확인
Launcher 사용 여부 확인
styles.css, icon.png가 jar 내부에 포함되어 있는지 확인

jar 내부 확인:

jar tf .\build\libs\FolderOrganizer-1.0.0.jar
5. Installer runs in background but does not appear

해결

기존 설치 제거
app-image 먼저 테스트
msi 생성 후 설치 테스트
필요 시 관리자 권한으로 실행
Distribution

친구나 지인에게 전달할 때는 dist 폴더 안의 설치 파일을 전달하면 됩니다.

예:

FolderOrganizer-1.0.0.msi
또는 FolderOrganizer-1.0.0.exe

권장:

최종 테스트 완료된 설치 파일 전달
간단한 사용법도 함께 전달

예시 안내 문구:

설치 후 실행하면 폴더 선택 → 목록 읽기 → 정리 실행 순서로 사용하면 됩니다.
정리 결과는 원본 폴더 옆에 "(폴더이름)_정리결과" 폴더로 생성되며,
되돌리기 기능도 지원합니다.

License

개인 프로젝트 및 포트폴리오용으로 제작되었습니다.
