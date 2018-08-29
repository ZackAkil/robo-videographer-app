
import numpy as np

class Temporal_Transformers:

    @staticmethod
    def do_all(x, y):
        print('stretching...')
        x_stretched, y_stretched = Temporal_Transformers.transform_stretch(x, y)
        print('diffing...')
        x_delta, y_delta = Temporal_Transformers.transform_to_delta(x_stretched, y_stretched)
        print('stacking...')
        x_stack, y_stack = Temporal_Transformers.transform_to_stack(x_delta, y_delta)
        print('done...')
        return x_stack, y_stack
        
        
    
    # stretch framerate (assume data is collected at 30fps)
    @staticmethod
    def transform_stretch(x, y, original_fps=30, new_fps=5):
        '''
        Increases time between frames, then stacks frames inorder to prevnt data loss.
        '''
        step_gap = int(original_fps/new_fps)
        x_stretched = np.zeros(x.shape)
        y_stretched = np.zeros(y.shape)

        prev_start = 0

        for step_start in range(step_gap):
            temp_x_stretched = x[step_start::step_gap]
            temp_y_stretched = y[step_start::step_gap]

            # print(temp_x_stretched.shape, prev_start, len(temp_x_stretched))

            slice_length = len(temp_x_stretched)

            x_stretched[prev_start : prev_start + slice_length] = temp_x_stretched
            y_stretched[prev_start : prev_start + slice_length] = temp_y_stretched
            prev_start += slice_length

        assert(len(x_stretched) == len(x))
        assert(len(x_stretched) == len(y_stretched))

        return x_stretched, y_stretched
    

    # delta images
    @staticmethod
    def transform_to_delta(x, y, thresh=0.015):
        x_delta = (x[:-1] - x[1:]) > thresh
        y_delta = y[1:]
        assert(len(x_delta)==len(y_delta))
        return x_delta, y_delta
    
    
    # image stacks
    @staticmethod
    def transform_to_stack(x, y, stack_size=5):
        new_len = len(x) - stack_size
        x_stack = np.zeros([new_len, stack_size, *x.shape[1:]])

        for i in range(new_len):
            x_stack[i] = x[i:i+stack_size]

        y_stack = y[stack_size:]

        assert(len(x_stack)==len(y_stack))

        return x_stack, y_stack