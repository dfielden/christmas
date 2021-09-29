package com.fielden.christmas_list;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootApplication
@Controller
public class ChristmasListApplication {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        SpringApplication.run(ChristmasListApplication.class, args);
    }

    @GetMapping("/")
    public String index(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return "index";
    }

    @GetMapping("/list")
    public String myList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return "mylist";
    }

    @ResponseBody
    @PostMapping("/add/{id}")
    public String addToList(@PathVariable(value="id") long id, HttpServletRequest req, HttpServletResponse resp, @RequestBody ItemInList item) {
        System.out.println(item);
        return gson.toJson(item);
    }
}
