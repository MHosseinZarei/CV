"""
URL configuration for GIS_Education_By_MHosseinZarei project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from django.conf import settings
from django.conf.urls.static import static
import FinalApp.views

# from FinalApp.views import MapView, ProvinceGeoJSONLayer


urlpatterns = [
    path('admin/', admin.site.urls),
    path('index/',FinalApp.views.index,name='index'),
    path('course/', FinalApp.views.course, name='courses'),
    path('signup/', FinalApp.views.SignupPage, name='signup'),
    path('login/', FinalApp.views.LoginPage, name='login'),
    path('logout/', FinalApp.views.LogoutPage, name='logout'),
    path('search/', FinalApp.views.search, name='search'),
    path('chart/', FinalApp.views.chart, name='chart'),
    path('rate_us/', FinalApp.views.rate_us, name='rate_us'),
    path('download_file/<int:CourseId>/', FinalApp.views.download_file, name='download_file'),
    path('map/', FinalApp.views.map, name='map'),
    path('searchmap/', FinalApp.views.searchmap, name='searchmap'),
    path('export-poi-shapefile/', FinalApp.views.export_poi_shapefile, name='export-poi-shapefile'),
    path('add-user-poi/', FinalApp.views.add_user_poi, name='add_user_poi'),    
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)