using System.Diagnostics;
using System.Net.Http;
using Microsoft.AspNetCore.Mvc;
using dotnetAuth.Models;
using Microsoft.AspNetCore.Mvc.Rendering;
using System.Text.Json;

namespace dotnetAuth.Controllers
{
    public class UserController : Controller
    {
        private readonly HttpClient _httpClient;

        public UserController(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }


        public async Task<IActionResult> Index()
        {
            var response = await _httpClient.GetAsync("http://localhost:8069/api/users/get");
            var users = await response.Content.ReadFromJsonAsync<List<User>>();
            return View(users);
        }
        [HttpGet]
        public async Task<IActionResult> Create()
        {
            try
            {
                var response = await _httpClient.GetAsync("http://localhost:8069/api/department/get");
                response.EnsureSuccessStatusCode();
                var departments = await response.Content.ReadFromJsonAsync<List<Department>>();
                ViewBag.Department = new SelectList(departments, "IdDepartment", "NameDepartment");
                // var departments = ViewBag.Department as SelectList ?? new SelectList(new List<Department>(), "Id", "Name");
                return View();
            }
            catch (JsonException ex)
            {
                // Tangani kesalahan JSON di sini, misalnya, log pesan kesalahan atau kembalikan respons yang sesuai kepada pengguna.
                // Contoh: return BadRequest("Gagal mendapatkan data departemen. Mohon coba lagi nanti.");
                return BadRequest("Gagal mendapatkan data departemen. Mohon coba lagi nanti.");
            }
        }


        [HttpPost]
        public async Task<IActionResult> Create(User user)
        {
            var response = await _httpClient.PostAsJsonAsync("http://localhost:8069/api/users/create", user);
            if (response.IsSuccessStatusCode)
            {
                return RedirectToAction("Index");
            }
            return View(user);
        }


    }

}
