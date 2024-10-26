
## 목차
 > 01. 프로젝트 소개
 > 02. 사용 기술
 > 03. 데이터베이스 구조
 > 04. 구현 기능

<br>
<br>

## ✅ 배포 사이트 👉 <a href="http://152.67.211.254:8080/" target="_blank">Together</a>

<br>
<br>


# 1️⃣ 1:N 매칭 서비스
![main](https://github.com/user-attachments/assets/5713c948-ce1c-411d-abdc-a2fa23c8cc25)

# 서비스 소개
> - 해당 프로젝트는 보육원의 아이들과 시니어를 매칭
> - 책읽기, 텃밭 가꾸기, 바둑 등 아이들과 함께할 수 있는 활동이 주를 이룸
> - 지역 사회의 연대와 참여 증진을 기대
> - 봉사자와 활동의 매칭을 통해 사회적 유대감을 높이고자 하는 목적
> - 노인과 어린이 간의 상호 작용을 통해 사회적 책임을 다 하는
  긍정적인 문화 확산을 기대

<br>

> ### 개발기간 및 인원
> 2024-08-23 ~ 2024-10-04 <br>
> 참여인원(5명)


<br>
<br>

# 사용 기술
> * ![JAVA](https://img.shields.io/badge/Java-007396?style=flat&logo=data:image/svg%2bxml;base64,PCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KDTwhLS0gVXBsb2FkZWQgdG86IFNWRyBSZXBvLCB3d3cuc3ZncmVwby5jb20sIFRyYW5zZm9ybWVkIGJ5OiBTVkcgUmVwbyBNaXhlciBUb29scyAtLT4KPHN2ZyB3aWR0aD0iMTUwcHgiIGhlaWdodD0iMTUwcHgiIHZpZXdCb3g9IjAgMCAzMi4wMCAzMi4wMCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiBmaWxsPSIjZmZmZmZmIiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMC4yNTYiPgoNPGcgaWQ9IlNWR1JlcG9fYmdDYXJyaWVyIiBzdHJva2Utd2lkdGg9IjAiLz4KDTxnIGlkPSJTVkdSZXBvX3RyYWNlckNhcnJpZXIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPgoNPGcgaWQ9IlNWR1JlcG9faWNvbkNhcnJpZXIiPiA8cGF0aCBmaWxsPSIjZmZmZmZmIiBkPSJNMTIuNTU3IDIzLjIyYzAgMC0wLjk4MiAwLjU3MSAwLjY5OSAwLjc2NSAyLjAzNyAwLjIzMiAzLjA3OSAwLjE5OSA1LjMyNC0wLjIyNiAwIDAgMC41OSAwLjM3IDEuNDE1IDAuNjkxLTUuMDMzIDIuMTU3LTExLjM5LTAuMTI1LTcuNDM3LTEuMjN6TTExLjk0MiAyMC40MDVjMCAwLTEuMTAyIDAuODE2IDAuNTgxIDAuOTkgMi4xNzYgMC4yMjQgMy44OTUgMC4yNDMgNi44NjktMC4zMyAwIDAgMC40MTEgMC40MTcgMS4wNTggMC42NDUtNi4wODUgMS43NzktMTIuODYzIDAuMTQtOC41MDgtMS4zMDV6TTE3LjEyNyAxNS42M2MxLjI0IDEuNDI4LTAuMzI2IDIuNzEzLTAuMzI2IDIuNzEzczMuMTQ5LTEuNjI1IDEuNzAzLTMuNjYxYy0xLjM1MS0xLjg5OC0yLjM4Ni0yLjg0MSAzLjIyMS02LjA5MyAwIDAtOC44MDEgMi4xOTgtNC41OTggNy4wNDJ6TTIzLjc4MyAyNS4zMDJjMCAwIDAuNzI3IDAuNTk5LTAuODAxIDEuMDYyLTIuOTA1IDAuODgtMTIuMDkxIDEuMTQ2LTE0LjY0MyAwLjAzNS0wLjkxNy0wLjM5OSAwLjgwMy0wLjk1MyAxLjM0NC0xLjA2OSAwLjU2NC0wLjEyMiAwLjg4Ny0wLjEgMC44ODctMC4xLTEuMDIwLTAuNzE5LTYuNTk0IDEuNDExLTIuODMxIDIuMDIxIDEwLjI2MiAxLjY2NCAxOC43MDYtMC43NDkgMTYuMDQ0LTEuOTV6TTEzLjAyOSAxNy40ODljMCAwLTQuNjczIDEuMTEtMS42NTUgMS41MTMgMS4yNzQgMC4xNzEgMy44MTQgMC4xMzIgNi4xODEtMC4wNjYgMS45MzQtMC4xNjMgMy44NzYtMC41MSAzLjg3Ni0wLjUxcy0wLjY4MiAwLjI5Mi0xLjE3NSAwLjYyOWMtNC43NDUgMS4yNDgtMTMuOTExIDAuNjY3LTExLjI3Mi0wLjYwOSAyLjIzMi0xLjA3OSA0LjA0Ni0wLjk1NiA0LjA0Ni0wLjk1NnpNMjEuNDEyIDIyLjE3NGM0LjgyNC0yLjUwNiAyLjU5My00LjkxNSAxLjAzNy00LjU5MS0wLjM4MiAwLjA3OS0wLjU1MiAwLjE0OC0wLjU1MiAwLjE0OHMwLjE0Mi0wLjIyMiAwLjQxMi0wLjMxOGMzLjA3OS0xLjA4MyA1LjQ0OCAzLjE5My0wLjk5NCA0Ljg4Ny0wIDAgMC4wNzUtMC4wNjcgMC4wOTctMC4xMjZ6TTE4LjUwMyAzLjMzN2MwIDAgMi42NzEgMi42NzItMi41MzQgNi43ODEtNC4xNzQgMy4yOTYtMC45NTIgNS4xNzYtMC4wMDIgNy4zMjMtMi40MzYtMi4xOTgtNC4yMjQtNC4xMzMtMy4wMjUtNS45MzQgMS43NjEtMi42NDQgNi42MzgtMy45MjUgNS41Ni04LjE3ek0xMy41MDMgMjguOTY2YzQuNjMgMC4yOTYgMTEuNzQtMC4xNjQgMTEuOTA4LTIuMzU1IDAgMC0wLjMyNCAwLjgzMS0zLjgyNiAxLjQ5LTMuOTUyIDAuNzQ0LTguODI2IDAuNjU3LTExLjcxNiAwLjE4IDAgMCAwLjU5MiAwLjQ5IDMuNjM1IDAuNjg1eiIvPiA8L2c+Cg08L3N2Zz4=) <img src="https://img.shields.io/badge/jQuery-0769AD?style=flat-square&logo=jQuery&logoColor=white"/> <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white"/> <img src="https://img.shields.io/badge/Javascript-ffb13b?style=flat-square&logo=javascript&logoColor=black"/> <img src="https://img.shields.io/badge/HTML-E34F26?style=flat-square&logo=HTML5&logoColor=white"/> <img src="https://img.shields.io/badge/CSS-1572B6?style=flat-square&logo=CSS3&logoColor=white"/>
> * <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=SpringBoot&logoColor=white"/> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=Gradle&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Oracle-F80000?style=flat-square&logo=Oracle&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Apachetomcat-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black"/> <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Intellij-000000?style=flat-square&logo=intellijidea&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Bootstrap-7952B3?style=flat-square&logo=bootstrap&logoColor=white"/>
> * <img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white"> <img src="https://img.shields.io/badge/Github-181717?style=flat-square&logo=github&logoColor=white">
  
<br>
<br>
<br>

# 아키텍처

<br>
<br>

![제목 없는 다이어그램 drawio](https://github.com/user-attachments/assets/150cc59d-a66a-42dc-bece-be7c93b95133)

<br>
<br>

<br>
<br>
<br>

# 데이터베이스 구조
![database](https://github.com/user-attachments/assets/abefaa23-8a15-49a4-afbc-6b9ca21f423f)
🔽🔽`Notification(알림) 추가 테이블`🔽🔽<br>
![database2](https://github.com/user-attachments/assets/ed6db29a-fc88-4838-aa88-bce06370cf24)

<br>
<br>
<br>
<hr>
<br>

![무제(1)](https://github.com/user-attachments/assets/372b233f-8c15-4438-8634-61601ce47000)

<br>
<br>


# ✅ 로그인
> * ### Ajax를 활용한 서버와의 비동기 통신으로 아이디, 비밀번호 검사
> * ### Spring Security 기반 접속 정보 인증과 권한부여
> * ### Security Remember-me 자동로그인
> * ### OAuth2 기반 소셜 로그인 연동
> * ### Cookie를 활용한 아이디 저장 기능
<br>

| ![로그인1](https://github.com/user-attachments/assets/8c3163c5-e417-4039-8a79-6263d083f643) | ![로그인2](https://github.com/user-attachments/assets/e46de3ae-7451-4fb7-83a9-a914bc19fca1) |
|:--:|:--:|
| <h3>로그인 페이지</h3> | <h3>일반회원 또는 소셜회원을 구분하여 사용자 안내</h3> |

<br>
<br>
<br>

# ✅ 회원가입
> * ### Ajax를 활용한 아이디 중복체크 및 전체 유효성 검사 실시
> * ### SMS API 사용: 문자인증 구현
> * ### MAP API 사용: 정확성 및 사용자 편의성 제공
> * ### FileReader API 사용: 첨부한 이미지 미리보기
<br>
<br>


|![회원가입1](https://github.com/user-attachments/assets/fe916e42-f5fe-4843-b993-30fd8fafc872)| ![회원가입2](https://github.com/user-attachments/assets/d8e4f1cc-da2f-4aed-954b-115f4456e833)|
|:--:|:--:|
|<h3>아이디 중복체크&문자인증</h3>|<h3>비밀번호 유효성 검사</h3>|

<br>
<br>
<br>

|![회원가입4](https://github.com/user-attachments/assets/3c7fa300-963c-4871-940f-7d2265d91da0)|
|:--:|
|<h3>Map Api</h3>|

<br>
<br>
<br>

|![회원가입5](https://github.com/user-attachments/assets/0f39b4bb-1f08-4be0-ad9d-cb3fd731bc27)|
|:--:|
|<h3>FileReader Api_이미지 미리보기</h3>|

<br>
<br>
<br>

# ✅ 카카오 소셜 회원가입
OAuth2 인증 요청 -> 응답객체 도착 -> 객체에서 'Profile Image Url' 추출 -> register Controller로 Url 및 필요 데이터 HttpSession을 통해 보내기
-> 받은 데이터를 기반으로 회원가입 진행 -> 'Profile Image Url'로 'Get' 요청 -> Image 다운로드 + 서버에 저장 로직 실행


<br>
<br>
<br>
<br>

# ✅ 비밀번호 재설정
> * ### Ajax를 활용한 아이디 존재유무 검사
> * ### 비밀번호 유효성 검사
<br>
<br>
<br>
<hr>

![비번찾기1_cropped](https://github.com/user-attachments/assets/7aee4869-ac96-483f-bb45-963b9523a4a7)

<hr>

|![비밀번호재설정1](https://github.com/user-attachments/assets/661f4340-a378-4f27-a040-cde8a7fe0b57)|![비밀번호재설정2](https://github.com/user-attachments/assets/705f5998-55de-4ad1-8648-85f66b95b553)|
|:--:|:--:|
|<h3>비밀번호/비밀번호확인 검사</h3>|<h3>비밀번호의 유효성 검사</h3>|

<br>
<br>
<br>





# ✅ 1:1 채팅
> * ### WebSocket을 활용한 1:1 채팅 구현
> * ### 최초 채팅방 입장시 상대방의 웰컴 메세지 전송
> * ### 기본적인 채팅방 목록, 삭제, 관리 기능
<br>

![채팅1](https://github.com/user-attachments/assets/4f878316-9e53-458e-9b9b-bfd7df31b73f)

<br>
<br>
<hr>
<br>
<br>

> * ### WebSocket을 활용한 실시간 채팅
> * ### 저장된 emitter를 조회, 상대방에게 즉시 알림 전송
> * ### 상대방이 이미 채팅방에 있는 경우 알림 전송 ❌
> * ### 상대방이 오프라인인 경우에도 알림 전송 ❌

<br>
<h3>채팅방 생성</h3>
  ⭐ 사용자는 상대방에게 채팅 상담을 요청할 시, 그 즉시 채팅방을 생성합니다.

<br>

1. 새로운 채팅방일 경우 상대방의 welcome message가 자동 발송됩니다.
2. 기존 채팅방이 존재하는 경우 해당 채팅방으로 자동 입장하게 됩니다.

<br>

<h3>채팅방 나가기</h3>
  ⭐ 사용자가 채팅방 나가기를 실행할 경우, 채팅방의 active를 변경하여 사용자에게 노출되지 않도록 합니다.

<br>
<br>
<br>

![채팅2](https://github.com/user-attachments/assets/b06c8659-2c80-4d56-a35b-d1376705e36a)

<br>
<br>
<br>

# ✅ 알림
> * ### SSE 방식의 실시간 채팅 알림 기능
> * ### 사이트 전역에서 toast 알림 전송
> * ### 채팅방에 같이 있다면 알림 ❌
> * ### 불필요한 알림을 최소화
<br>

<h3>채팅방 생성</h3>
  ⭐ 사용자는 상대방에게 채팅 상담을 요청할 시, 그 즉시 채팅방을 생성합니다.

<br>

1. 상대방이 채팅방에 입장한 상태라면, 상대방에게 불필요한 알림을 보내지 않습니다.
2. 채팅방에 접속한 상태인 경우, 이를 구분하여 알림과 메세지 읽음 처리가 실시간으로 가능하게 구현하였습니다.

<br>
<br>
<br>

![알림1](https://github.com/user-attachments/assets/8caacaa2-6e6f-407d-be08-7bd21415fad4)  ⬅⬅⬅

<br>
<br>
<br>


# ✅ 최신 모집글
> * ### 최신 글 순으로 20개 진열
> * ### 도/시 구 까지 주소 간략 표시

<br>

![main_최근모집글](https://github.com/user-attachments/assets/1e44fa08-7d3f-49b9-861c-acd83adf8459)






