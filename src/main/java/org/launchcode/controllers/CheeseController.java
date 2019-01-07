package org.launchcode.controllers;

import org.launchcode.models.Category;
import org.launchcode.models.Cheese;
import org.launchcode.models.User;
import org.launchcode.models.data.CategoryDao;
import org.launchcode.models.data.CheeseDao;
import org.launchcode.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by LaunchCode
 */
@Controller
@RequestMapping("cheese")
public class CheeseController {

    @Autowired
    private CheeseDao cheeseDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private UserDao userDao;

    // Request path: /cheese
    @RequestMapping(value = "")
    public String index(Model model, @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        model.addAttribute("cheeses", user.getCheeses());
        model.addAttribute("title", "My Cheeses");

        return "cheese/index";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddCheeseForm(Model model, @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        model.addAttribute("title", "Add Cheese");
        model.addAttribute(new Cheese());
        model.addAttribute("categories", user.getCategories());
        return "cheese/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddCheeseForm(@ModelAttribute  @Valid Cheese newCheese,
                                       Errors errors,
                                       Model model,
                                       @RequestParam int categoryId,
                                       @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        if (errors.hasErrors()) {
            model.addAttribute("title", "Add Cheese");
            model.addAttribute("categories", categoryDao.findAll());
            return "cheese/add";
        }

        newCheese.setUser(user);
        Category cat = categoryDao.findOne(categoryId);
        newCheese.setCategory(cat);
        cheeseDao.save(newCheese);
        return "redirect:";
    }

    @RequestMapping(value = "edit/{cheeseId}", method = RequestMethod.GET)
    public String displayEditForm(Model model, @PathVariable int cheeseId, @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        Cheese currentCheese = cheeseDao.findOne(cheeseId);
        model.addAttribute("cheese", currentCheese);
        model.addAttribute("categories", user.getCategories());
        model.addAttribute("title", "Edit Cheese " + currentCheese.getName() + " (" + cheeseId + ")");
        return "cheese/edit";

    }

    @RequestMapping(value = "edit/{cheeseId}", method = RequestMethod.POST)
    public String processEditForm(@PathVariable int cheeseId, String name, String description, int categoryId, Integer rating, @ModelAttribute @Valid Cheese currentCheese,
                                  Errors errors, Model model, @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        if (errors.hasErrors()) {
            model.addAttribute("categories", user.getCategories());
            model.addAttribute("title", "Edit Cheese " + cheeseDao.findOne(cheeseId).getName() + " (" + cheeseId + ")");
            return "cheese/edit";
        }

        currentCheese = cheeseDao.findOne(cheeseId);

        currentCheese.setName(name);
        currentCheese.setDescription(description);

        Category cat = categoryDao.findOne(categoryId);
        currentCheese.setCategory(cat);

        currentCheese.setRating(rating);

        cheeseDao.save(currentCheese);

        return "redirect:/cheese";
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String displayRemoveCheeseForm(Model model, @CookieValue(value = "user", defaultValue = "none") String username) {

        if (username.equals("none")) {
            return "redirect:/user/login";
        }

        User user = userDao.findByUsername(username).get(0);
        model.addAttribute("cheeses", user.getCheeses());
        model.addAttribute("title", "Remove Cheese");
        return "cheese/remove";
    }

    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public String processRemoveCheeseForm(@RequestParam int[] cheeseIds) {

        for (int cheeseId : cheeseIds) {
            cheeseDao.delete(cheeseId);
        }

        return "redirect:";
    }

    @RequestMapping(value = "category", method = RequestMethod.GET)
    public String category(Model model, @RequestParam int id) {
        Category cat = categoryDao.findOne(id);
        List<Cheese> cheeses = cat.getCheeses();
        model.addAttribute("cheeses", cheeses);
        model.addAttribute("title", "Cheeses in Category: " + cat.getName());
        return "cheese/index";
    }

}
