from math import ceil
from google.cloud import storage
from google.oauth2 import service_account
import os
from PIL import Image


class Downloader:

    def __init__(self):
        credentials = service_account.Credentials.from_service_account_file("./dore-tours-vandy-d94b025a43ae.json")
        scoped_credentials = credentials.with_scopes(['https://www.googleapis.com/auth/cloud-platform'])

        storage_client = storage.Client(credentials=scoped_credentials)
        # The name for the new bucket
        bucket_name = 'dore-tours-vandy.appspot.com'

        # Creates the new bucket
        self._bucket = storage_client.get_bucket(bucket_name)

    def get_directories(self):
        unique_directories = set()
        for blob in self._bucket.list_blobs():
            unique_directories.add(blob.name.split("/")[0])
        image_directories = [x for x in unique_directories]
        return image_directories

    def get_images(self, directory):
        return [self._bucket.get_blob(blob.name).download_as_string() for blob in self._bucket.list_blobs(prefix=directory)]

    def save_images(self, directory):
        path = os.getcwd() + "/Data/"
        if os.path.exists(path + directory):
            current_imgs = os.listdir(path + directory)
        else:
            os.mkdir(path + directory)
            current_imgs = []
        for blob in self._bucket.list_blobs(prefix=directory):
            if blob.name.split("/")[1] not in current_imgs:
                self._bucket.blob(blob.name).download_to_filename(path + blob.name)


class ImageManager:

    PORTION_TRAINING = 0.8

    IMAGE_SIZE = 240

    def __init__(self, percent_training=PORTION_TRAINING):
        self._downloader = Downloader
        self._percent_training = percent_training

    def _divide_images(self, imgs):
        num_training = ceil(len(imgs) * self._percent_training)
        training_count = 0
        training = []
        testing = []
        for img in imgs:
            if training_count < num_training:
                training.append(img)
                training_count += 1
            else:
                testing.append(img)
        return training, testing

    def _download_directory(self, directory_name):
        return self._downloader.get_images(directory_name)

    # Returns dict {dir_name: [img_paths]}
    @staticmethod
    def get_images_on_disk():
        imgs = {}
        path = os.getcwd() + "/Data"
        for o in os.listdir(path):
            cur_path = path + "/" + o
            if os.path.isdir(cur_path):
                imgs[o] = [Image.open(cur_path + "/" + img) for img in os.listdir(cur_path) if img.split(".")[1] == "jpg"]
        return imgs

    def get_image_sets_from_download(self):
        image_sets = []
        for directory_name in self._downloader.get_directories():
            training, testing = self._divide_images(self._download_directory(directory_name))
            image_sets.append(ImageSet(directory_name, training, testing))
        return image_sets

    def get_image_sets_from_disk(self):
        images = ImageManager.get_images_on_disk()
        image_sets = []
        for dir_name in images:
            training, testing = self._divide_images(images[dir_name])
            image_sets.append(ImageSet(dir_name, training, testing))
        return image_sets


class ImageSet:

    def __init__(self, name, training_set, testing_set):
        self._name = name
        self._training = training_set
        self._testing_set = testing_set

    def get_training_data(self):
        return self._training

    def get_testing_data(self):
        return self._testing_set

    def get_name(self):
        return self._name

