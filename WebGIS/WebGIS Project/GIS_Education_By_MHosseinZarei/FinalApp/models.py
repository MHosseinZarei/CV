from __future__ import unicode_literals
from django.core.validators import MinValueValidator, MaxValueValidator
from django.db import models
from django.contrib.gis.db import models
from django.db.models import Manager as GeoManager

#Define Student Class
class Student(models.Model):
    StudentId       = models.IntegerField()
    StudentName     = models.CharField(max_length=50)
    StudentLastName = models.CharField(max_length=50)
    StudentBirthday = models.DateField()
    StudentCourses  = models.CharField(max_length=50)
    StudentScorses  = models.FloatField()
    Datejoined      = models.DateField()
    Description     = models.CharField(max_length=500)
    def __str__(self):
        return self.StudentName
    

##Define Course Class
class Course(models.Model):
    CourseId       = models.IntegerField()
    title          = models.CharField(max_length=100)
    description    = models.TextField()
    start_date     = models.DateField()
    end_date       = models.DateField()
    participants   = models.IntegerField()
    CourseFile     = models.FileField(upload_to='.')
    CourseImg      = models.FileField(upload_to='.')

    
    def __str__(self):
        return self.title
    

##Define UserRating Class
class UserRating(models.Model):
    rating = models.IntegerField(validators=[MinValueValidator(0), MaxValueValidator(100)])
    created_at = models.DateTimeField(auto_now_add=True)
    rater_name = models.CharField(max_length=255)
    rater_email = models.EmailField()


##Define POI Class
class POI(models.Model):
    name = models.CharField(max_length=20)
    location = models.PointField(max_length=20)
    objects = GeoManager()
    def __unicode__(self):
        return self.name
    class Meta:
        verbose_name_plural = "POIs"




##Define Prvnc Class
class Prvnc(models.Model):
    name_1 = models.CharField(max_length=254)
    humidity = models.FloatField()
    precipitat = models.FloatField()
    elevation_field = models.FloatField()
    temperatur = models.FloatField()
    population = models.FloatField()
    website = models.CharField(max_length=50)
    نام_ف = models.CharField(max_length=50)
    geom = models.MultiPolygonField(srid=4326)
    def __unicode__(self):
        return self.name
    
    

##Define cities Class
class cities(models.Model):
    objectid_1 = models.BigIntegerField()
    area = models.FloatField()
    perimeter = models.FloatField()
    provincnam = models.CharField(max_length=20)
    cityname = models.CharField(max_length=25)
    shape_leng = models.FloatField()
    shape_area = models.FloatField()
    geom = models.MultiPolygonField(srid=4326)
    def __unicode__(self):
        return self.name
    



##Define UserPOI Class
class UserPOI(models.Model):
    name = models.CharField(max_length=200)
    location = models.PointField()

    def __str__(self):
        return self.name
