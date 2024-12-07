weolbu-test api server
====

# 빌드 방법

```shell
./gradlew build
```

# 어플리케이션 실행 방법

- server-application

```shell
./gradlew :weolbu-api-server:bootRun
```

- swagger

```shell
open http://localhost:8080/swagger/index.html
```

- h2-console (id: `sa`, password: 없음)

```shell
open http://localhost:8080/h2-console
```

# weolbu-test API

## ErrorResponse

- API 요청이 실패한 경우, ResponseBody 로 ErrorResponse 응답해요

| Property Name  | Optional | Type   | description                   | example        |
|----------------|----------|--------|-------------------------------|----------------|
| code           | X        | String | 에러코드. Client 에서 분기처리 하기 위한 목적 | USR1001        |
| displayMessage | X        | String | 사용자에게 노출되는 메시지                | 이미 등록된 이메일이에요. |
| details        | X        | String | 에러 상세 내용(debug message)       | 01012345678    |

## 1. 회원가입 API. `POST - /user-accounts`

- HTTP Body Parameters

| Parameter Name | Optional | type         | description                    | example              |
|----------------|----------|--------------|--------------------------------|----------------------|
| name           | X        | String       | 이름                             | 홍길동                  |
| email          | X        | String       | 이메일                            | woods@fake.naver.com |
| phoneNumber    | X        | String       | 휴대폰 번호                         | 01012345678          |
| userType       | X        | String(enum) | 비밀번호                           | password             |
| password       | X        | String       | 회원 유형(`INSTRUCTOR`, `STUDENT`) | INSTRUCTOR           | 

- curl command

```shell
curl -X 'POST' \
  'http://localhost:8080/user-accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "홍길동",
  "email": "gildong@fake.naver.com",
  "phoneNumber": "01012345678",
  "userType": "INSTRUCTOR",
  "password": "password1"
}'
```

- success response - 200 OK

```json
{
  "userAccountId": 2
}
```

- failure response - 422 UnprocessableEntity + ErrorResponse

```json
{
  "code": "USR1001",
  "displayMessage": "이미 등록된 이메일이에요.",
  "details": "The email is already registered. email=gildong@fake.naver.com"
}
```

## 2. 강의 개설 API. `POST - /courses`

- HTTP Body Parameters

| Parameter Name  | Optional | type    | description | example       |
|-----------------|----------|---------|-------------|---------------|
| userAccountId   | X        | String  | 회원 계정 식별자   | 1             |
| title           | X        | String  | 강의명         | 너나위의 내집마련 기초반 |
| maxParticipants | X        | Integer | 최대 수강 인원    | 10            |
| price           | X        | Integer | 가격          | 10000         |

- curl command

```curl
curl -X 'POST' \
  'http://localhost:8080/courses' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "userAccountId": 2,
  "title": "너나위의 내집마련 기초반",
  "maxParticipants": 10,
  "price": 10000
}'
```

- success response - 204 NoContent
- failure response - 422 UnprocessableEntity + ErrorResponse

```json
{
  "code": "COR1002",
  "displayMessage": "강사 권한이 필요한 기능이에요.",
  "details": "Course creation failed. User is not an instructor. userAccountId=3"
}
```

3. 강의 조회 API. `GET - /courses`

- HTTP Query Parameters

| Parameter Name | Optional | type         | description                                                              | example         |
|----------------|----------|--------------|--------------------------------------------------------------------------|-----------------|
| page           | O        | Integer      | 페이지 번호                                                                   | 1               |
| size           | O        | Integer      | 페이지 크기                                                                   | 20              |
| sort           | O        | String(enum) | 정렬(`RECENTLY_REGISTERED`, `MOST_APPLICANTS`, `HIGHEST_APPLICATION_RATE`) | MOST_APPLICANTS |

- curl command

```curl
curl -X 'GET' \
  'http://localhost:8080/courses?page=1&size=20&sort=RECENTLY_REGISTERED' \
  -H 'accept: */*'
```

- success response - 200 OK

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "totalElements": 2,
  "items": [
    {
      "id": 2,
      "title": "너나위의 내집마련 기초반",
      "maxParticipants": 10,
      "price": 10000,
      "createdAt": "2024-12-01T14:52:26.546338Z",
      "currentParticipants": 0,
      "registrationRate": 0
    },
    {
      "id": 1,
      "title": "title",
      "maxParticipants": 1,
      "price": 1000,
      "createdAt": "2024-12-01T14:38:49.823241Z",
      "currentParticipants": 1,
      "registrationRate": 100
    }
  ]
}
```

4. 수강 신청 API. `POST - /courses/registration`

- HTTP Body Parameters

| Parameter Name | Optional | type        | description         | example   |
|----------------|----------|-------------|---------------------|-----------|
| userAccountId  | X        | String      | 회원 계정 식별자           | 1         |
| courseIds      | X        | String List | 수강신청 하려는 강의 ID List | [1, 2, 3] |

- curl command

```curl
curl -X 'POST' \
  'http://localhost:8080/courses/registration' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "userAccountId": 1,
  "courseIds": [
    1, 2, 3
  ]
}'
```

- success response - 200 OK

```json
{
  "results": [
    "강의등록에 실패했어요. - [너나위의 내집마련 기초반2] 최대 수강 인원 도달로 인해 수강 신청에 실패했어요.",
    "[courseId=2] 강의등록에 성공했어요.",
    "강의등록에 실패했어요. - [courseId=3] 요청한 강의를 찾지 못했어요."
  ]
}
```
