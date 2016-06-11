using Microsoft.Owin;
using Owin;

[assembly: OwinStartupAttribute(typeof(ShoppingMall.Startup))]
namespace ShoppingMall
{
    public partial class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            ConfigureAuth(app);
        }
    }
}
