from tkinter import *
import tkinter
from PIL import Image, ImageTk
from os import listdir
import pandas as pd
import os.path
import time

import skvideo.io
import skvideo.datasets

video_file_name = '../video/raw/VID_20180325_143338.mp4'
video_data = skvideo.io.vread(video_file_name)

print('video data ', video_data.shape)

x,y = 0,0
image_index = 0
stop = True
speed = 500

class Application(Frame):

    def createWidgets(self):
        self.QUIT = Button(self)
        self.QUIT["text"] = "QUIT"
        self.QUIT["fg"]   = "red"
        self.QUIT["command"] =  self.quit

        self.QUIT.pack({"side": "left"})


    def __init__(self, master=None):
        Frame.__init__(self, master)
        self.pack()
        self.createWidgets()



def motion(event):
    global x, y
    x, y = event.x, event.y


def next_image():
    global image_index, x, y, stop, speed

    display_image(image_index)
    image_index+=1
    w.delete("all")
    w.create_image(0, 0, anchor=tkinter.NW, image=current_photo)
    w.create_line(x, 0, x, 450,fill="red")
    w.create_line(0, y, 650, y,fill="red")
    print(x,y)

    if image_index >= len(video_data):
        print("finished data")
    elif not stop:
        w.after(speed, next_image)

def play(event):
    global stop 
    if stop:
        stop = False
        next_image()
    


def pause(event):
    global stop 
    stop = True


def display_image(image_index):
    global current_photo
    current_photo = ImageTk.PhotoImage(Image.fromarray(video_data[image_index].astype('uint8'), 'RGB'))
 
def key(event):
    global speed 

    if event.char == '\uf703':
        print('right')
        speed = max(speed-50, 10)


    elif event.char == '\uf702':
        print('left')
        speed += 50

    elif event.char == '\uf701':
        print('down')

    else:
        print("pressed", repr(event.char))


root = Tk()

current_photo = None

w = Canvas(root, width=1000, height=1000)
w.pack()

w.bind('<Motion>', motion)
w.bind("<Button-1>", play)
w.bind("<Button-2>", pause)
root.bind("<Key>", key)


app = Application(master=root)
app.mainloop()
root.destroy()

