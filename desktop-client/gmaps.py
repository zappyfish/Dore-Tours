import googlemaps
import urllib
from PIL import Image
from io import BytesIO
import numpy
import math


class GoogleMaps:

    BASE_URL = 'http://maps.google.com/maps/api/staticmap?'

    def __init__(self):
        self.gmaps = googlemaps.Client(key='AIzaSyCOioRVNUprjXD-Puy4PWZUr_6MmTBeL3c')
        self.zoom = 17
        self.w = 600
        self.h = 600
        self.lat_center = -1000
        self.lon_center = -1000

    def fetch_campus_image(self, name):
        if self.lat_center == -1000 or self.lon_center == -1000:
            search_results = self.gmaps.geocode(address=name)[0]['geometry']['location']
            self.lat_center = search_results['lat']
            self.lon_center = search_results['lng']
        return self._get_image()

    def _get_image(self):
        position = ','.join((str(self.lat_center), str(self.lon_center)))
        urlparams = urllib.parse.urlencode({'center': position,
                                            'zoom': str(self.zoom),
                                            'size': str(self.w) + 'x' + str(self.h),
                                            'sensor': 'false',
                                            'maptype': 'roadmap',
                                            'scale': '1',
                                            'key': 'AIzaSyCOioRVNUprjXD-Puy4PWZUr_6MmTBeL3c'})
        url = GoogleMaps.BASE_URL + urlparams
        image_result = urllib.request.urlopen(url)
        image = Image.open(BytesIO(image_result.read()))
        return numpy.array(image)

    def update_image(self, x_r=0, y_r=0, zoom=0):
        x = (x_r + 0.5) * self.w
        y = (y_r + 0.5) * self.h
        self.lat_center, self.lon_center = self.get_point_lat_lng(x, y)
        self.zoom += zoom
        return self._get_image()

    def get_point_lat_lng(self, x, y):
        parallel = math.cos(self.lat_center * math.pi / 180)
        degrees_per_pixel_x = 360 / math.pow(2, self.zoom + 8)
        degrees_per_pixel_y = 360 / math.pow(2, self.zoom + 8) * parallel
        point_lat = self.lat_center - degrees_per_pixel_y * (y - self.h / 2)
        point_lng = self.lon_center + degrees_per_pixel_x * (x - self.w / 2)

        return point_lat, point_lng


