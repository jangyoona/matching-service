# 참여 프로젝트
>  * 1️⃣  1:N 매칭 서비스 : `은퇴한 시니어와 보육원의 아이들을 매칭하여 재능기부(봉사활동)을 목적으로 제작한 웹서비스`
>  * 2️⃣  그룹웨어 서비스 : `기업에서 필요한 사내 관리 웹서비스`
<br>
<br>

## 목차
 > 01. 프로젝트 소개
 > 02. 사용 기술
 > 03. 데이터베이스 구조
 > 04. 구현 기능

<br>
<br>


# 1️⃣ 1:N 매칭 서비스
![main](https://github.com/user-attachments/assets/f174afb6-9608-40fd-b7db-82051fb0a3a2)

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

# 데이터베이스 구조
![database](https://github.com/user-attachments/assets/0830d794-9ddd-4e9b-8db5-289562deb1a8)
🔽🔽`Notification(알림) 추가 테이블`🔽🔽<br>
![database2](https://github.com/user-attachments/assets/fb66b1fb-c2b9-41f5-9b27-b64a52443b9c)

<br>
<br>

# 구현기능
<br>
<br>
<hr>
<hr>


# ✅ 로그인
> * ### Ajax를 활용한 서버와의 비동기 통신으로 아이디, 비밀번호 검사
> * ### Spring Security 기반 접속 정보 인증과 권한부여
> * ### Security Remember-me 자동로그인
> * ### OAuth2 기반 소셜 로그인 연동
> * ### Cookie를 활용한 아이디 저장 기능
<br>

| ![로그인1](https://github.com/user-attachments/assets/95b9f9bc-47d4-47fb-b7fc-93d2b7709bea) | ![로그인2](https://github.com/user-attachments/assets/03e38332-e521-4cd1-8428-d9bf4d5e6192) |
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

| ![회원가입](https://github.com/user-attachments/assets/214b9203-8164-4683-a2e6-c853e1a5a977) | ![회원가입2](https://github.com/user-attachments/assets/9bc12c94-b3c0-42f2-88d2-f17c3b4dab95) |
|:--:|:--:|
| <h3>아이디 중복체크&문자인증 / 이미지 미리보기</h3> | <h3>첨부파일 / 필수약관</h3> |

<br>
<br>
<br>

# ✅ 1:1 채팅
> * ### WebSocket을 활용한 1:1 채팅 구현
> * ### 최초 채팅방 입장시 상대방의 웰컴 메세지 전송
> * ### 기본적인 채팅방 목록, 삭제, 관리 기능
<br>

![채팅1](https://github.com/user-attachments/assets/f78e66ec-e8bd-4352-ae0c-31395b0a570a)

<br>
<br>
<hr>
<br>
<br>

> * ### WebSocket을 활용한 실시간 채팅
> * ### 저장된 emitter를 조회, 상대방에게 즉시 알림 전송
> * ### 상대방이 이미 채팅방에 있는 경우 알림 전송 ❌
> * ### 상대방이 오프라인인 경우에도 알림 전송 ❌
![채팅2](https://github.com/user-attachments/assets/95588859-fa34-4f4d-998a-9729c7b6dbdb)

<br>
<br>
<br>

# ✅ 알림
> * ### SSE 방식의 실시간 채팅 알림 기능
> * ### 사이트 전역에서 toast 알림 전송
> * ### 채팅방에 같이 있다면 알림 ❌
> * ### 불필요한 알림을 최소화
<br>

![알림1](https://github.com/user-attachments/assets/9bbb848e-c388-4f40-899f-a21a3ef69630)  ⬅⬅⬅

<br>
<br>
<br>







