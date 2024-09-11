from FinalApp.models import Student
from FinalApp.models import Course
from FinalApp.models import UserRating 
from FinalApp.models import cities
from django.shortcuts import render,HttpResponse,redirect
from django.contrib.auth.models import User
from django.contrib.auth import authenticate,login,logout
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.urls import reverse
from django.http import FileResponse
from django.shortcuts import get_object_or_404
from django.core.exceptions import ValidationError
from django.views.generic import TemplateView
from django.core.serializers import serialize
from .models import Prvnc,POI
from django.contrib.gis.geos import Point
from django.contrib.gis.geos import Polygon
from django.views.generic import View
from django.contrib.gis.db.models.functions import Distance
from django.contrib.gis.geos import fromstr
from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse
from .models import UserPOI
import json
from shapely.geometry import shape, Point
import os
import tempfile
import geopandas as gpd
from shapely.geometry import Point
import shutil
from djgeojson.views import GeoJSONLayerView

##index
@login_required(login_url='login')
def index(request):
    show_admin_tab = request.user.is_superuser
    show_CEO_tab = request.user.groups.filter(name='CEO').exists()
    
    #Statistics information
    total_years    = 4 
    total_courses  = Course.objects.count()
    total_students = Student.objects.count()
    total_projects = 80  
    context = {
        'show_admin_tab': show_admin_tab,
        'show_CEO_tab': show_CEO_tab,
        'total_years': total_years,
        'total_courses': total_courses,
        'total_students': total_students,
        'total_projects': total_projects,
    }


    return render(request, 'FinalApp/index.html', context)

##course
@login_required(login_url='login')
def course(request):
    courses = Course.objects.all()  
    return render(request, 'FinalApp/course.html', {'courses': courses})



##Signup
def SignupPage(request):
    if request.method=='POST':
        uname=request.POST.get('username')
        email=request.POST.get('email')
        pass1=request.POST.get('password1')
        pass2=request.POST.get('password2')

        if pass1!=pass2:
            messages.error(request, "Your password and confirm password are not Same!!")
        else:

            my_user=User.objects.create_user(uname,email,pass1)
            my_user.is_active= True
            my_user.save()
            login(request,my_user)
            return redirect('index')
    return render (request,'FinalApp/signup.html')


##Login
def LoginPage(request):
    if request.method=='POST':
        username=request.POST.get('username')
        pass1=request.POST.get('pass')
        user=authenticate(request,username=username,password=pass1)
        if user is not None:
            login(request,user)
            return redirect('index')
        else:
            messages.error(request, "Username or Password is incorrect!!!")
    return render (request,'FinalApp/login.html')

def LogoutPage(request):
    logout(request)
    return redirect('login')


##search
@login_required(login_url='login')
def search(request):
    if request.method == "POST":
        searched = request.POST.get('searched', '')
        if searched:
            courses = Course.objects.filter(title__iexact=searched)  
            return render(request, 'FinalApp/search.html',{'searched':searched,'courses':courses})
        else:
            return render(request, 'FinalApp/index.html',{})
        


##chart for admin
@login_required(login_url='login')
def chart(request):
    is_superuser = request.user.is_superuser
    courses = Course.objects.all()
    course_label = [course.title for course in courses]
    course_data = [course.participants for course in courses]
    Students= Student.objects.all()
    student_label = [Student.StudentName for Student in Students]
    student_data = [Student.StudentScorses for Student in Students]
    if not is_superuser:
        return redirect('index')
    
    return render(request, 'FinalApp/chart.html', {
        'course_label': course_label,
        'course_data': course_data,
        'student_label': student_label,
        'student_data': student_data,
    })


##DownLoad
@login_required(login_url='login')
def download_file(request, CourseId):

    course = get_object_or_404(Course, pk=CourseId)

    file_path = course.CourseFile.path

    response = FileResponse(open(file_path, 'rb'))
    
    response['Content-Type'] = 'application/octet-stream'
    response['Content-Disposition'] = f'attachment; filename="{course.CourseFile.name}"'
    
    return response


##UserRating
@login_required
def rate_us(request):
    error_message = None
    user_rating   = None
    
    if request.method == 'POST':
        rating = request.POST.get('rating')
        user_name = request.user.username
        user_email = request.user.email

        try:
            rating = int(rating)
            if not 0 <= rating <= 100:
                raise ValidationError("Score must be between 0 and 100.")
            
            UserRating.objects.create(rating=rating, rater_name=user_name, rater_email=user_email)
            user_rating = rating 
        except ValueError:
            error_message = "Please enter a valid number."
        except ValidationError as ve:
            error_message = str(ve.message)

    courses = Course.objects.all()
    return render(request, 'FinalApp/course.html', {'courses': courses, 'error_message': error_message, 'user_rating': user_rating})




##Map
@login_required
def map(request):
    # Retrieve province geometries
    city_objects = cities.objects.all()
    
    # Construct GeoJSON data for cities
    geojson_data = {
        'type': 'FeatureCollection',
        'features': []
    }
    for city in city_objects:
        geojson_data['features'].append({
            'type': 'Feature',
            'geometry': json.loads(city.geom.geojson),
            'properties': {
                'objectid_1': city.objectid_1,
                'area': city.area,
                'perimeter': city.perimeter,
                'provincnam': city.provincnam,
                'cityname': city.cityname,
                'shape_leng': city.shape_leng,
                'shape_area': city.shape_area,
            }
        })

    # Retrieve all POIs
    pois = POI.objects.all()

    # Prepare GeoJSON data for POIs
    geojson_data1 = {
        'type': 'FeatureCollection',
        'features': []
    }

    for poi in pois:
        geojson_data1['features'].append({
            'type': 'Feature',
            'geometry': {
                'type': 'Point',
                'coordinates': [poi.location.x, poi.location.y] 
            },
            'properties': {
                'name': poi.name
            }
        })

    # Pass GeoJSON data to the template
    context = {
        'geojson_data': json.dumps(geojson_data),
        'geojson_data1': json.dumps(geojson_data1),
    }
    return render(request, 'FinalApp/map.html', context)



##searchmap
@login_required(login_url='login')
def searchmap(request):
    if request.method == "POST":
        searchedmap = request.POST.get('searchedmap', '')
        if searchedmap:
            try:
                poi = POI.objects.get(name__icontains=searchedmap)
                return render(request, 'FinalApp/map.html', {'poi': poi})
            except POI.DoesNotExist:
                return render(request, 'FinalApp/map.html', {})
        else:
            # Handle empty search input if needed
            return render(request, 'FinalApp/map.html', {})
    else:
        return render(request, 'FinalApp/map.html', {})



##Export shapefile
def export_poi_shapefile(request):
    # Create a temporary directory
    temp_dir = tempfile.TemporaryDirectory()
    temp_path = temp_dir.name

    # Get POI data
    pois = POI.objects.all()
    
    # Prepare data for GeoPandas
    data = {
        'name': [poi.name for poi in pois],
        'geometry': [Point(poi.location.x, poi.location.y) for poi in pois]
    }
    gdf = gpd.GeoDataFrame(data, crs="EPSG:4326")

    # Path to save shapefile
    shapefile_path = os.path.join(temp_path, 'pois.shp')
    
    # Export to shapefile
    gdf.to_file(shapefile_path)

    # Create a zip file
    zip_path = os.path.join(temp_path, 'pois.zip')
    with tempfile.TemporaryDirectory() as temp_zip_dir:
        temp_zip_path = os.path.join(temp_zip_dir, 'pois')
        gdf.to_file(temp_zip_path)
        shutil.make_archive(temp_path + '/pois', 'zip', temp_zip_path)

    # Read the zip file
    with open(zip_path, 'rb') as f:
        zip_data = f.read()

    # Create response
    response = HttpResponse(zip_data, content_type='application/zip')
    response['Content-Disposition'] = 'attachment; filename="pois.zip"'

    # Clean up temporary files
    temp_dir.cleanup()

    return response



##Add poi by user
def add_user_poi(request):
    if request.method == 'POST':
        name = request.POST.get('name')
        lat = request.POST.get('lat')
        lng = request.POST.get('lng')

        # Save user_poi to database
        user_poi = UserPOI(name=name, location=f'POINT({lng} {lat})')
        user_poi.save()

        return JsonResponse({'status': 'success'})  # Return success JSON response

    return JsonResponse({'status': 'error'}, status=400)  # Return error JSON response if method is not POST
