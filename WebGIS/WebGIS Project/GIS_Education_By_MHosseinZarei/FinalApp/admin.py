from django.contrib import admin
from .models import POI,Prvnc,cities

from FinalApp.models import Student
from FinalApp.models import Course
from FinalApp.models import UserRating

from leaflet.admin import LeafletGeoAdmin
class POIsAdmin(LeafletGeoAdmin):
    list_display = ('name','location')
admin.site.register(POI,POIsAdmin)
class PrvncsAdmin(LeafletGeoAdmin):
    list_display = ('name_1','نام_ف')
admin.site.register(Prvnc,PrvncsAdmin)

class citiesAdmin(LeafletGeoAdmin):
    list_display = ('cityname','provincnam')
admin.site.register(cities,citiesAdmin)
admin.site.register(Student)
admin.site.register(Course)
admin.site.register(UserRating)