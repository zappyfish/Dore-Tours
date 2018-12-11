from gmaps import GoogleMaps
import cv2

gmap = GoogleMaps()

img = gmap.fetch_campus_image("Vanderbilt University")

print("w: %dh: %d" % img.shape)

cv2.imshow('fetched', img)

p1 = gmap.get_point_lat_lng(0, 0)
p2 = gmap.get_point_lat_lng(0, 599)

print("(%f, %f); (%f, %f)" % (p1[0], p1[1], p2[0], p2[1]))

cv2.waitKey(0)

img = gmap.update_image(x_r=0.1, y_r=0.1)

cv2.imshow('fetched', img)

cv2.waitKey(0)
