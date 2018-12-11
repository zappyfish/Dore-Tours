from preprocess import preprocess_map
import cv2
from firebase import FirebaseManager
import numpy as np
from math import floor

class ImageContourer:

    def __init__(self, img, campus, building_name, gmaps, num_segments=1):
        self.img = preprocess_map(img)
        w, h = self.img.shape
        self.display_img = np.zeros((w, h, 3), np.uint8)
        for i in range(3):
            self.display_img[:, :, i] = self.img
        self.x = -1
        self.y = -1
        self.campus = campus
        self.name = building_name
        self.coords = []
        self.mousedown = False
        self.complete = False
        self.fbm = FirebaseManager()
        self.gmaps = gmaps
        self.window = 'Contouring '
        if building_name is not None:
            self.window += building_name
        else:
            self.window += campus
        self.contour_count = 0
        self.num_segments = num_segments

    def set_complete(self):
        self.complete = True

    def start(self):
        cv2.imshow(self.window, self.display_img)
        cv2.setMouseCallback(self.window, ImageContourer.mouseclick, self)
        k = 0
        while not self.is_complete():
            k = cv2.waitKey(50) & 0xFF
        cv2.destroyWindow(self.window)

    @staticmethod
    def mouseclick(event, x, y, flags, self):
        if event == cv2.EVENT_LBUTTONDOWN and not self.complete:
            self.mousedown = not self.mousedown
            if not self.mousedown:
                self.upload_results()
        elif event == cv2.EVENT_MOUSEMOVE and self.mousedown:
            self.coords.append((x, y))
            self.draw_coords()

    def draw_coords(self):
        l = len(self.coords)
        if l > 1:
            p1 = self.coords[l - 1]
            p2 = self.coords[l - 2]
            cv2.line(self.display_img, (p1[0], p1[1]), (p2[0], p2[1]), (0, 255, 0), 5)
        cv2.imshow(self.window, self.display_img)
        cv2.waitKey(1)

    def is_complete(self):
        return self.complete or self.contour_count >= self.num_segments

    def upload_results(self):
        results = {}
        cnt = 0
        sample_rate = int(floor(len(self.coords) / 100.0))
        if sample_rate == 0:
            sample_rate = 1
        for i, coord in enumerate(self.coords):
            if i % sample_rate == 0:
                results[cnt] = {}
                lat, lon = self.gmaps.get_point_lat_lng(coord[0], coord[1])
                results[cnt]['lat'] = lat
                results[cnt]['lon'] = lon
                cnt += 1
        if self.name is not None:
            self.fbm.set(['campuses', self.campus, 'buildings', self.name, 'coordinates'], results)
        else:
            self.fbm.set(['campuses', self.campus, 'coordinates', str(self.contour_count)], results)
        self.contour_count += 1
        self.coords.clear()
