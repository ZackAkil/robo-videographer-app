
import pandas as pd
from scipy import misc
from skimage.transform import resize
import matplotlib.pyplot as plt
import numpy as np

class Image_Fetcher():
    
    
    def __init__(self, image_path, label_path, crop_zone):
        
        self.image_path = image_path
        self.labels = pd.DataFrame.from_csv(label_path)
        self.crop_zone = crop_zone
        self.raw_image_size = misc.imread(self.image_path + self.labels.iloc[0].name, flatten=True).shape
        
        self.show_test_image()
        
        
    def show_test_image(self):
        
        test_img = misc.imread(self.image_path + self.labels.iloc[0].name, flatten=True)
        plt.imshow(test_img[self.crop_zone[0]:self.crop_zone[1]])
        plt.show()
        
        
    def image_resize(self, img, resize_shape ):
        return resize(image=(img[self.crop_zone[0]:self.crop_zone[1]]/(255/2))-1, output_shape=resize_shape)
    
    def get_all_images_with_labels(self, resize_shape = (16,50), msg=True):
        
        m = len(self.labels)
        
        final_image_size = resize_shape
        
        data_shape = [m, *final_image_size]

        X_data = np.zeros(data_shape)
        
        y_data = []
        
        count = 0
        
        prev_img = None


        for image_file_name, y_label in self.labels.iterrows():

            if np.isnan(y_label['y']):
                prev_img = None
                if msg:
                    print('current image is none')
            else:
                
                if not(count % 100):
                    print(count)
                    
                current_image = misc.imread(self.image_path + image_file_name, flatten=True)
                
                new_img = self.image_resize(current_image, resize_shape)
                
                if prev_img is not None:
                    delta_img = np.clip((prev_img - new_img),a_min=0, a_max=1)
                    X_data[count,:,:] =  delta_img
                    
                    y_data.append(y_label['y']/ self.raw_image_size[1])
                    
                    count += 1
                    if count >= m:
                        break
                else:
                    if msg:
                        print('prev image is none')
                prev_img = new_img

        return X_data[:count], np.array(y_data)
    
    
    @staticmethod
    def time_stack_data(x, y, time_frames=2):
        
        phase = time_frames - 1
        
        chron_X = np.zeros([x.shape[0]-phase, time_frames, *x.shape[1:]])
        chron_y = np.array(y[phase:])

        # stack the input frames in sequencial groups
        for i in range(2):
            end = -(phase - i) 
            end = None if end == 0 else end
            chron_X[:,i,:,:] = x[i: end ]

        print(chron_X.shape, chron_y.shape)
        return chron_X, chron_y
        
        
    @staticmethod
    def show_frame_sequence(x, y, i):
    
        for ith_frame in range(x.shape[1]):
            
            plt.imshow(x[i, ith_frame, :, :])
            plt.show()

        correct_output = np.zeros(x[i,0,:,:].shape)
        correct_output[:,int(y[i]* correct_output.shape[1])] = 1

        plt.imshow(correct_output)
        plt.show()

        print(y[i])
    
    