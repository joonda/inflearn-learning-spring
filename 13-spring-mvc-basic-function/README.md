## 노트

### 1. 프로젝트 생성

- Dependencies
  - spring-web
  - thymeleaf
  - lombok

### 2. 로깅 간단히 알아보기

- 운영 시스템에서는 `System.out.println()` 같은 시스템 콘솔을 사용해서 필요한 정보를 출력하지 않고, 별도의 로깅 라이브러리를 사용해서 로그를 출력

#### `로깅 라이브러리`

- SLF4J - http://www.slf4j.org
- Logback - http://logback.qos.ch
- SLF4J 는 통합으로 인터페이스로 제공하는 라이브러리
  - 즉 SLF4J는 인터페이스, 그 구현체로 Logback 같은 로그 라이브러리를 선택

* `springmvc` > `basic`
  - `LogTestController`

```java
// ... 생략
@RestController
public class LogTestController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/log-test")
    public String logTest() {

        String name = "Spring";

        log.trace("trace log = {}", name);
        log.debug("debug log = {}", name);
        log.info("info log = {}", name);
        log.warn("warn log = {}", name);
        log.error("warn log = {}", name);

        return "ok";
    }
}

```

- `@RestController`

  - `return` 에 있는 `String`이 그대로 반환이 된다
  - REST API를 만들 때 핵심적인 컨트롤러이다.

- `application.properties`

```
# 전체 로그 레벨 설정 (기본은 info)
logging.level.root=info

#hello.springmvc 패키지와 그 하위 로그 레벨 설정
#logging.level.hello.springmvc=debug
```

- 로컬 PC 에서는 `trace`, 개발 서버에서는 `debug`, 운영 서버에서는 `info` 등 다르게 설정할 수 있음
  - 대부분 다르게 설정해서 하는 것이 일반적
- `logging.level.root`로 전체 로그 레벨을 설정

  - 기본 값은 `info`이며, 원하는 패키지만 debug or trace 등으로 지정하는 것이 일반적

- `@RestController`
  - `@Controller`는 반환 값이 `String` 이면 `View` 이름으로 인식된다 그래서 `View`를 찾고, `View`가 렌더링
  - `@RestController`는 반환 값으로 `View`를 찾는 것이 아니라 `HTTP 메시지 바디에 바로 입력`한다.
    따라서 실행 결과로 ok 메시지 반환, `@ResponseBody`와 관련이 있음

```java
// ... 생략
@Slf4j
@RestController
public class LogTestController {

    @GetMapping("/log-test")
    public String logTest() {

        String name = "Spring";

        log.trace("trace log = {}", name);
        log.debug("debug log = {}", name);
        log.info("info log = {}", name);
        log.warn("warn log = {}", name);
        log.error("warn log = {}", name);

        return "ok";
    }
}
```

- lombok 에서 제공하는 `@Slf4j` Annotation으로 `log`를 사용할 수 있게 편의성을 제공함.

#### `로그 사용시 장점`

- 로그 레벨에 따라 개발 서버, 운영 서버 등에서 다르게 조절할 수 있다
- 파일이나 네트워크 등 로그를 별도의 위치에 남길 수 있다.
- 성능도 매우 우수, 실무에서는 무조건 로그 사용

### 3. 요청 매핑

```java
// ... 생략
@RestController
public class MappingController {

        private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = {"/hello-basic", "/hello-go"}, method = RequestMethod.GET)
    public String helloBasic() {
        log.info("hello-basic");
        return "ok";
    }

    @RequestMapping(value = "/mapping-get-v1", method = RequestMethod.GET)
    public String mappingGetV1() {
        log.info("mappingGetV1");
        return "ok";
    }

    @GetMapping(value = "/mapping-get-v2")
    public String mappingGetV2() {
        log.info("mappingGetV2");
        return "ok";
    }

    /**
     * PathVariable 사용
     * 변수명이 같으면 생략 가능
     * @PathVariable(userId) String userId -> PathVariable userId
     * /mapping/userA
     */
    @GetMapping("/mapping/{userId}")
    public String mappingPath(@PathVariable String userId) {
        log.info("mappingPath userId = {}", userId);

        return "ok";
    }

    /**
     * PathVariable 복수개
     * @param userId -> userA
     * @param orderId -> 100
     * /mapping/users/userA/orders/100
     */
    @GetMapping("/mapping/users/{userId}/orders/{orderId}")
    public String mappingPath2(@PathVariable String userId, @PathVariable String orderId) {
        log.info("mappingPath2 userId = {}, orderId = {}", userId, orderId);

        return "ok";
    }

    /**
     * 특정 Param으로 매핑
     * params = "mode",
     * params = "!mode",
     * params = "mode=debug"
     * params = "mode!=debug",
     * params = {"mode=debug", "data=good"}
     * 특정 파라미터가 오지 않으면 400 Error (Bad Request)
     */
    @GetMapping(value = "/mapping-param", params = "mode=debug")
    public String mappingParam() {
        log.info("mappingParam");
        return "ok";
    }

    /**
     * 특정 header로 매핑
     * headers = "mode",
     * headers = "!mode",
     * headers = "mode=debug"
     * headers = "mode!=debug",
     * headers = {"mode=debug", "data=good"}
     */
    @GetMapping(value = "/mapping-header", headers = "mode=debug")
    public String mappingHeader() {
        log.info("mappingHeader");
        return "ok";
    }

    /**
     * Content-Type 헤더 기반 추가 매핑 (Media Type)
     * consumes = "application/json"
     * consumes = "!application/json"
     * consumes = "application/*"
     * consumes = "*\/*"
     * mediaType.APPLICATION_JSON_VALUE
     */
    @PostMapping(value = "/mapping-consume", consumes = "application/json")
    public String mappingConsume() {
        log.info("mappingConsume");
        return "ok";
    }

    /**
     * Accept 헤더 기반 Media Type
     * produces = "text/html"
     * produces = "!text/html"
     * produces = "text/*"
     * produces = "*\/*"
     */
    @PostMapping(value = "/mapping-produce", produces = "text/html")
    public String mappingProduces() {
        log.info("mappingProduces");
        return "ok";
    }
}
```

- Mapping은 배열로 받을 수 있음 (복수 가능)
- `method`를 지정할 시, 해당 요청 제외는 405 error 반환
- params, headers, consumes, produces 등 여러가지 요청을 매핑할 수 있다

### 4. 요청 매핑 - API 예시

#### `회원관리 API`

- 회원 목록 조회: GET `/users`
- 회원 등록: POST `/users`
- 회원 조회: GET `/users/{userId}`
- 회원 수정: PATCH `/users/{userId}`
- 회원 삭제: DELETE `/users/{userId}`

```java
// ... 생략
@RestController
@RequestMapping("/users")
public class MappingClassController {

    @GetMapping
    public String user() {
        return "get users";
    }

    @PostMapping
    public String addUser() {
        return "post user";
    }

    @GetMapping("/{userId}")
    public String findUser(@PathVariable String userId) {
        return "get userId " + userId;
    }

    @PatchMapping("/{userId}")
    public String updateUser(@PathVariable String userId) {
        return "update userId " + userId;
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId) {
        return "delete userId " + userId;
    }
}
```

### 5. HTTP 요청 - 기본, 헤더 조회

```java
@Slf4j
@RestController
public class RequestHeaderController {

    @RequestMapping("/headers")
    public String headers(HttpServletRequest request,
                          HttpServletResponse response,
                          HttpMethod httpMethod,
                          Locale locale,
                          @RequestHeader MultiValueMap<String, String> headerMap,
                          @RequestHeader("host") String host,
                          @CookieValue(value = "myCookie", required = false) String cookie
                          ) {
        log.info("request = {}", request);
        log.info("response = {}", response);
        log.info("request method = {}", httpMethod);
        log.info("locale = {}", locale);
        log.info("headerMap = {}", headerMap);
        log.info("host = {}", host);
        log.info("cookie = {}", cookie);

        return "ok";
    }
}
```

- MultiValueMap
  - Map과 유사한데, 하나의 키에 여러 값을 받을 수 있다.

### 6. HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form

#### HTTP 요청 데이터 조회 - 개요

- HTTP 요청 메시지를 통해 클라이언트에서 서버로 데이터를 전달하는 방법을 알아보자.

- #### 클라이언트 -> 서버 요청 데이터를 전달할 때는 주로 다음 3가지 방법을 사용
  - `GET - 쿼리 파라미터`
    - /url **?username=hello&age=20**
    - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함시켜 전달
    - 검색, 필터, 페이징 등에서 많이 사용
  - `POST - HTML Form`
    - content-type: application/x-www-form-urlencoded
    - 메시지 바디에 쿼리 파라미터 형식으로 전달 username=hello&age=20
    - 회원 가입, 상품 주문, HTML Form 사용
  - `HTTP message body`에 데이터를 직접 담아 요청
    - `HTTP API`에서 주로 사용, `JSON`, `XML`, `TEXT`
    - 데이터 형식은 주로 `JSON` 사용
    - `POST`, `PUT`, `PATCH`

* `hello` > `springmvc` > `basic` > `request`
  - `RequestParamController`

```java
// ... 생략
@Slf4j
@Controller
public class RequestParamController {

    @RequestMapping("/request-param-v1")
    public void requestParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username =  request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        log.info("username = {}, age = {}", username, age);

        response.getWriter().write("ok");
    }
}
```

-

* `main` > `resources` > `static` > `basic`
  - `hello-form.html`

```html
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Title</title>
  </head>
  <body>
    <form action="/request-param-v1" method="post">
      username: <input type="text" name="username" /> age:<input
        type="text"
        name="age"
      />
      <button type="submit">전송</button>
    </form>
  </body>
</html>
```

- `http://localhost:8080/basic/hello-form.html` 엔드포인트에서 `username`, `age` 정보를 입력 후 전송
  - `/request-param-v1`으로 이동하며 (Form의 영향) `username`, `age`의 정보가 `log`로 찍힌다
- 또는 `http://localhost:8080/request-param-v1?username=Lee&age=20` 이렇게 쿼리 파라미터로 엔드포인트를 이동해도 log가 찍힌다.
