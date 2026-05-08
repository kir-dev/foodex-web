package hu.kirdev.foodex.config

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RedirectController {

    @GetMapping("/")
    fun redirectToHomepage(): String {
        return "redirect:/api/homepage"
    }
}