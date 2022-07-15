package tests;

import data.*;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static specification.Specification.*;

public class TestSuit {

    @Feature("Проверка уникальности имен файлов аватаров пользователей")
    @Test
    public void testAvatarFileNames() {
        installSpec(requestSpec(),responseSpec());

        Resource resource = given()
                .when()
                .get("/api/users?page=2")
                .then()
                .log().body()
                .extract().body().as(Resource.class);
        List<String> compareList = new ArrayList<>();
        resource.getData().forEach(x->compareList.add(x.getAvatar().replace("https://reqres.in/img/faces/", "").replace(".jpg", "")));
        List<String> compareListDistinct = compareList.stream().distinct().collect(Collectors.toList());
        Assert.assertEquals(compareList.size(), compareListDistinct.size(),
                "Имена файлов аватаров пользователей не уникальны");

        deleteSpec();
    }

    @Feature("Проверка успешной авторизации пользователя в системе")
    @Test(dataProvider = "authorisationSuccess")
    public void testAuthorisationSuccess(String email, String password) {
        Login login = new Login(email, password);
        LoginSuccessful loginSuccessful = given()
                .contentType("application/json")
                .body(login)
                .when()
                .post("https://reqres.in/api/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().as(LoginSuccessful.class);
        Assert.assertFalse(loginSuccessful.getToken().isEmpty(),
                "Успешная авторизация не выполнена, токен не получен");
    }

    @DataProvider(name = "authorisationSuccess")
    public Object[][] sendLoginPassword() {
        return new Object[][] {
                {"eve.holt@reqres.in", "cityslicka"}
        };
    }

    @Feature("Проверка неудачной авторизации пользователя в системе из-за невведенного пароля")
    @Test(dataProvider = "authorisationNotSuccess")
    public void testAuthorisationNotSuccess(String email) {
        Login login = new Login(email);
        LoginUnsuccessful loginUnsuccessful = given()
                .contentType("application/json")
                .body(login)
                .when()
                .post("https://reqres.in/api/login")
                .then()
                .log().all()
                .statusCode(400)
                .extract().body().as(LoginUnsuccessful.class);
        Assert.assertEquals(loginUnsuccessful.getError(), "Missing password",
                "Полученная причина ошибки не совпадает с требуемой");
    }

    @DataProvider(name = "authorisationNotSuccess")
    public Object[][] sendLoginWithoutPassword() {
        return new Object[][] {
                {"peter@klaven"}
        };
    }

    @Feature("Проверка сортировки операции LIST <RESOURCE>")
    @Test
    public void testListResource() {
        Resource resource = given()
                .when()
                .get("https://reqres.in/api/unknown")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().as(Resource.class);
        List<Integer> compareList = new ArrayList<>();
        resource.getData().forEach(x->compareList.add(x.getYear()));
        for (int i = 0; i < compareList.size() - 1; i++) {
            if (compareList.get(i) > compareList.get(i + 1))
                Assert.fail("Данные не отсортированы по годам");
        }
    }

    @Feature("Проверка количества тегов в сервисе Gateway AutoDNS")
    @Test(dataProvider = "tagsAmount")
    public void testTagsAmount(int tagsAmount) {
        Response response = given()
                .when()
                .get("https://gateway.autodns.com/")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();
        String xmlDocument = response.asString().replace("/", "");
        List<String> xmlTagsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("<.+?>");
        Matcher matcher = pattern.matcher(xmlDocument);
        while (matcher.find()) {
            xmlTagsList.add(xmlDocument.substring(matcher.start(), matcher.end()));
        }
        Assert.assertEquals(xmlTagsList.size() / 2, tagsAmount,
                "Количество тегов не равно 14");
    }

    @DataProvider(name = "tagsAmount")
    public Object[][] checkTagsAmount() {
        return new Object[][] {
                {14}
        };
    }
}
