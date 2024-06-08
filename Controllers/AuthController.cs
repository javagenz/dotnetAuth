using System.Net.Http;
using System.Net.Http.Json;
using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json.Linq;

namespace SIPN.Controllers;

public class AuthController : Controller
{
    private readonly HttpClient _httpClient;

    public AuthController(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    [HttpPost("/Home/Login-Action")]
    public async Task<IActionResult> SignIn(LoginModel model)
    {
        // Assuming you have an API endpoint for authentication
        var response = await _httpClient.PostAsJsonAsync("http://localhost:8191/auth/signin", model);
        if (response.IsSuccessStatusCode)
        {
            // Authentication successful, set up the authentication cookie
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, model.Email)
            };

            var claimsIdentity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);

            await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(claimsIdentity));

            TempData["LoginMessage"] = "Login Successful";
            TempData["status"] = "true";
            return RedirectToRoute(new { controller = "Profile", action = "Index" });
        }
        else
        {
            // Authentication failed, read error message from API response
            var errorContent = await response.Content.ReadAsStringAsync();
            var errorMessage = JObject.Parse(errorContent)["message"]?.ToString(); // Adjust parsing as per your API response format

            TempData["LoginMessage"] = errorMessage ?? "Login Failed.";
            TempData["status"] = "false";
            return RedirectToRoute(new { controller = "Home", action = "Login" });
        }
    }

    [HttpPost("/Home/Register-Action")]
    public async Task<IActionResult> SignUp(RegisterModel model)
    {
        model.Role = "ADMIN";
        // Assuming you have an API endpoint for authentication
        var response = await _httpClient.PostAsJsonAsync("http://localhost:8191/auth/signup", model);
        if (response.IsSuccessStatusCode)
        {
            // Authentication successful, set up the authentication cookie
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, model.Email)
            };

            var claimsIdentity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);

            await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(claimsIdentity));

            TempData["LoginMessage"] = "Register Successful";
            TempData["status"] = "true";
            return RedirectToRoute(new { controller = "Profile", action = "Index" });
        }
        else
        {
            // Authentication failed, read error message from API response
            var errorContent = await response.Content.ReadAsStringAsync();
            var errorMessage = JObject.Parse(errorContent)["message"]?.ToString(); // Adjust parsing as per your API response format

            TempData["LoginMessage"] = errorMessage ?? "Register Failed.";
            TempData["status"] = "false";
            return RedirectToRoute(new { controller = "Home", action = "R egister" });
        }
    }

    [HttpPost]
    public async Task<IActionResult> Logout()
    {
        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        TempData["LoginMessage"] = "Logged Out Successfully.";
        TempData["status"] = "false";
        return RedirectToAction("index", "Home");
    }
}
