package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class ShoppingCartTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://demowebshop.tricentis.com";
        Configuration.startMaximized = true;
        Configuration.baseUrl = "http://demowebshop.tricentis.com";
    }

    @Test
    public void addItemToCartAsNewUserTest() {
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&product_attribute_72_3_20=57&addtocart_72.EnteredQuantity=1")
                .when()
                .post("/addproducttocart/details/72/1")
                .then()
                .statusCode(200)
                .log().body()
                .body("success", is(true))
                .body("updatetopcartsectionhtml", is("(1)"));
    }

    @Test
    void addItemToCartAsExistingUserTest() {
        // request cart size
        Response response = given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&product_attribute_72_3_20=57&addtocart_72.EnteredQuantity=1")
                .cookie("Nop.customer=6247dace-0353-40b3-a1d6-9ddef6df77af; ARRAffinity=06e3c6706bb7098b5c9133287f2a8d510a64170f97e4ff5fa919999d67a34a46; __utma=78382081.235830450.1621354488.1621354488.1621354488.1; __utmc=78382081; __utmz=78382081.1621354488.1.1.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utmt=1; NopCommerce.RecentlyViewedProducts=RecentlyViewedProductIds=72; __atuvc=1%7C20; __atuvs=60a3e806b9795c5a000; __utmb=78382081.2.10.1621354488")
                .when()
                .post("/addproducttocart/details/72/1")
                .then()
                .statusCode(200)
                .log().body()
                .body("success", is(true))
                .extract().response();

        String cartSizeInBrackets = response.jsonPath().get("updatetopcartsectionhtml");
        int cartSize = Integer.parseInt(cartSizeInBrackets.substring(1, cartSizeInBrackets.length() - 1));

        // expected cart size +=1
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&product_attribute_72_3_20=57&addtocart_72.EnteredQuantity=1")
                .cookie("Nop.customer=6247dace-0353-40b3-a1d6-9ddef6df77af; ARRAffinity=06e3c6706bb7098b5c9133287f2a8d510a64170f97e4ff5fa919999d67a34a46; __utma=78382081.235830450.1621354488.1621354488.1621354488.1; __utmc=78382081; __utmz=78382081.1621354488.1.1.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utmt=1; NopCommerce.RecentlyViewedProducts=RecentlyViewedProductIds=72; __atuvc=1%7C20; __atuvs=60a3e806b9795c5a000; __utmb=78382081.2.10.1621354488")
                .when()
                .post("/addproducttocart/details/72/1")
                .then()
                .statusCode(200)
                .log().body()
                .body("success", is(true))
                .body("updatetopcartsectionhtml", is("(" + (cartSize + 1) + ")"));
    }

    @Test
    void addItemToCartAsExistingUserAndCheckOnUITest() {
        open("/build-your-cheap-own-computer");
        $("#add-to-cart-button-72").click();
        $("#topcartlink a[href='/cart']").shouldHave(text("(1)"));

        String nopCustomerCookie = WebDriverRunner.getWebDriver().manage().getCookieNamed("Nop.customer").getValue();
        System.out.println("Nop.customer = " + nopCustomerCookie);

        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&product_attribute_72_3_20=57&addtocart_72.EnteredQuantity=1")
                .cookie("Nop.customer", nopCustomerCookie)
                .when()
                .post("/addproducttocart/details/72/1")
                .then()
                .statusCode(200)
                .log().body()
                .body("success", is(true))
                .body("updatetopcartsectionhtml", is("(2)"));

        Selenide.refresh();
        $("#topcartlink a[href='/cart']").shouldHave(text("(2)"));
    }
}
