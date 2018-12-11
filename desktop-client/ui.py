from tkinter import *
import sys
import cv2
from gmaps import GoogleMaps
from contour import ImageContourer


class mainWindow(object):
    def __init__(self,master):
        self.gmaps = GoogleMaps()
        self.master=master
        self.e1 = Entry()
        self.e1.pack()
        self.b=Button(master,text="search campus",command=self.search)
        self.b.pack()
        self.num_regions = Entry()
        self.num_regions.pack()
        self.campus = Button(master,text='perform campus segmentation',command=self.segment_campus)
        self.campus.pack()
        self.e2 = Entry()
        self.e2.pack()
        self.b2=Button(master,text="segment building",command=self.segment_building)
        self.b2.pack()
        self.b3=Button(master,text='zoom in', command=self.zoom_in)
        self.b3.pack()
        self.b4 = Button(master, text='zoom out', command=self.zoom_out)
        self.b4.pack()
        self.up = Button(master,text='move up', command=self.move_up)
        self.up.pack()
        self.down = Button(master, text='move down', command=self.move_down)
        self.down.pack()
        self.left = Button(master, text='move left', command=self.move_left)
        self.left.pack()
        self.right = Button(master, text='move right', command=self.move_right)
        self.right.pack()
        self.campus_contourer = None
        self.done = None

    def segment_campus(self):
        self.done = Button(self.master, text='done with campus', command=self.set_done)
        self.done.pack()
        campus = self.e1.get()
        img = self.gmaps.update_image()
        num_segments = 1
        try:
            num_segments = int(self.num_regions.get())
        except:
            pass
        self.campus_contourer = ImageContourer(img, campus, None, self.gmaps, num_segments)
        self.campus_contourer.start()

    def set_done(self):
        if self.campus_contourer is not None:
            self.campus_contourer.set_complete()
            self.campus_contourer = None
            self.done.pack_forget()
            self.done = None

    def move_up(self):
        self.move(0, -0.1)

    def move_down(self):
        self.move(0, 0.1)

    def move_left(self):
        self.move(-0.1, 0)

    def move_right(self):
        self.move(0.1, 0)

    def move(self, x=0.0, y=0.0):
        val = self.e1.get()
        if val is not None and val != "":
            img = self.gmaps.update_image(x_r=x, y_r=y, zoom=0)
            cv2.imshow(val, img)
            cv2.waitKey(1)

    def zoom_in(self):
        val = self.e1.get()
        if val is not None and val != "":
            img = self.gmaps.update_image(x_r=0,y_r=0,zoom=1)
            cv2.imshow(val, img)
            cv2.waitKey(1)

    def zoom_out(self):
        val = self.e1.get()
        if val is not None and val != "":
            img = self.gmaps.update_image(x_r=0, y_r=0, zoom=-1)
            cv2.imshow(val, img)
            cv2.waitKey(1)

    def segment_building(self):
        campus = self.e1.get()
        img = self.gmaps.update_image()
        ic = ImageContourer(img, campus, self.e2.get(), self.gmaps)
        ic.start()

    def search(self):
        val = self.e1.get()
        img = self.gmaps.fetch_campus_image(val)
        cv2.imshow(val, img)
        cv2.waitKey(1)


if __name__ == "__main__":
    root=Tk()
    m=mainWindow(root)
    root.mainloop()