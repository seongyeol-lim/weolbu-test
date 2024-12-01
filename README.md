weolbu-test api-server
====

# 빌드 방법

```shell
./gradlew build
```

# 어플리케이션 실행 방법

```shell
./gradlew :weolbu-api-server:bootRun
```

# weolbu-test API

## Swagger

```shell
./gradlew :weolbu-api-server:bootRun
open http://localhost:8080/swagger-ui.html
```

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

- Success Response - 204 NoContent
- Failure Response - 422 UnprocessableEntity + ErrorResponse

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

- success response
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
