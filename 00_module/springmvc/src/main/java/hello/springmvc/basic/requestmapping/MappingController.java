package hello.springmvc.basic.requestmapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

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
