from images import Downloader

dl = Downloader()

for directory in dl.get_directories():
    dl.save_images(directory)