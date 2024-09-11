import os
from django.contrib.gis.utils import LayerMapping
from .models import Prvnc
from .models import cities
Prvncs_mapping = {
    'name_1': 'Name_1',
    'humidity': 'Humidity',
    'precipitat': 'Precipitat',
    'elevation_field': 'Elevation_',
    'temperatur': 'Temperatur',
    'population': 'population',
    'website': 'Website',
    'نام_ف': 'نام_ف',
    'geom': 'MULTIPOLYGON',
}
Prvncs_shp = os.path.abspath('F:\_OTHER\shapefiles\Ostan_iran\Ostan_iran.shp')
def run(verbose=True):
    lm=LayerMapping(Prvnc,Prvncs_shp,Prvncs_mapping, transform=False,encoding='utf8')
    lm.save(strict=True,verbose=verbose)



cities_mapping = {
    'objectid_1': 'OBJECTID_1',
    'area': 'area',
    'perimeter': 'perimeter',
    'provincnam': 'ProvincNam',
    'cityname': 'CityName',
    'shape_leng': 'Shape_Leng',
    'shape_area': 'Shape_Area',
    'geom': 'MULTIPOLYGON',
}
cities_shp = os.path.abspath('F:\_OTHER\shapefiles\Shahrestan\Shahrestan.shp')
def run(verbose=True):
    lm=LayerMapping(cities,cities_shp,cities_mapping, transform=False,encoding='utf8')
    lm.save(strict=True,verbose=verbose)