
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class aws_gatling_project extends Simulation {

//	val baseURL = "https://www.academy.com"
//	val Users  = 1
// 	val Duration  = 30
// 	val ThinkTime = 2
//	val RampUp = 3
	
	val baseURL = System.getProperty("URL")
	val Users  = System.getProperty("Users").toInt
  	val Duration  = System.getProperty("Test_Duration").toInt
  	val ThinkTime = System.getProperty("ThinkTime").toInt
	val RampUp = System.getProperty("RampUp_Time").toInt
	
	val pdp_feed = csv("pdp.csv").random

	val httpProtocol = http
		.baseUrl(baseURL)
		.acceptHeader("*/*")
		.acceptLanguageHeader("en")
		//.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", //""".*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())
		.acceptEncodingHeader("gzip, deflate")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36")

	val headers_0 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Connection" -> "keep-alive",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_1 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Connection" -> "keep-alive",
		"Content-Type" -> "application/json;charset=UTF-8",
		"Origin" -> baseURL)
		
	
	object Homepage {
		val homepage = exec(http("Homepage")
				        .get("/")
						.check(status.is(200))
				        .headers(headers_0))
	}
	
	object Browse {
		val CLP = exec(http("CLP")
			.get("/shop/browse/apparel/mens-apparel")
			.check(status.is(200))
			.headers(headers_0))
			
		val PLP = exec(http("PLP")
			.get("/shop/browse/apparel/mens-apparel/mens-shirts--t-shirts")
			.check(status.is(200))
			.headers(headers_0))

		val PDP = feed(pdp_feed)
			.exec(http("PDP")
			.get("${pdp_url}")
//			.get("/shop/pdp/magellan-outdoors%E2%84%A2-mens-pecos-ridge-short-sleeve-shirt")
			.check(status.is(200))
			.headers(headers_0))
	}
	
	object Cart {
		val addtocart = exec(http("AddToCart")
				        .post("/api/cart/sku")						 .body(StringBody("""{"skus":[{"id":"4958063","quantity":1,"type":"REGULAR"}],"giftAmount":"","inventoryCheck":true,"isGCItem":false}""")).asJson
						.check(status.is(200))
						.headers(headers_1))
		
		val viewcart = exec(http("ViewCart")	
					   .get("/webapp/wcs/stores/servlet/ShopCartView?ddkey=https:AYOrderItemDisplay&langId=-1&currency=USD&catalogId=10551&storeId=10151")	
					   .check(status.is(200))
					   .headers(headers_0))					   
	}
	
	val cart_scenario = scenario("CartOperations")
    .forever {
      exec(flushHttpCache)
//        .doIf(CartOperations == "true") {
          exec(
            Homepage.homepage).pause(ThinkTime)
            .exec(Browse.CLP).pause(ThinkTime)
            .exec(Browse.PLP).pause(ThinkTime)
            .exec(Browse.PDP).pause(ThinkTime)
//            .exec(Cart.addtocart).pause(ThinkTime)
//            .exec(Cart.viewcart).pause(ThinkTime)
            .exec(flushSessionCookies)
            .pause(ThinkTime)
//        }
    }

	setUp(
	cart_scenario.inject(atOnceUsers(1))
//	cart_scenario.inject(rampUsers(Users) during (RampUp seconds))
	)
	.protocols(httpProtocol)
	.maxDuration(Duration seconds)
}