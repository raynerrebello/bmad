from PIL import Image
import numpy as np
import pprint as pp
from palette import web_palette
from matplotlib.pyplot import imsave
import bitarray as bt
pad = lambda x :'0'*(8-len(x)) + x if len(x) < 8 else x

# Returns a 2-d np-array of length 8 strings for every pixel
def img2bin(fn):
        im = Image.open(fn)
        arr = np.array(im)
        bin_repr = [[pad(bin(pixel).lstrip('0b')) for pixel in row] for row in arr]
        return np.array(bin_repr)

# Returns an PIL Image object from an array of 8-bit binary strings
def bin2img(bin_repr,palette = web_palette):
        arr = np.array([[int(str(pixel), 2) for pixel in row] for row in bin_repr],dtype = 'uint8')
        im = Image.fromarray(arr,mode='P')
        im.putpalette(palette)
        im.show()
        return im

def img2mat(fn):
        x = img2bin(fn)
        print(x[0,0])
        r = np.zeros((*x.shape,8),dtype = 'uint8')
        for i,row in enumerate(x):
                for j,val in enumerate(row):
                        r[i,j,:] = np.array([int(d) for d in list(val)])
        r = r.reshape(r.shape[0], 8*r.shape[1])
        n,m = r.shape
        r = r.reshape(r.shape[0]*r.shape[1])
        r = list(r)
        with open(f"./out/{fn.split('.')[1].split('/')[-1]}_{n}_{m}.bin",'wb') as f:
                bt.bitarray(r).tofile(f)
        print(r[0:8])

def bin2img(fn):
        f= open(fn,'rb')

        k = bt.bitarray(endian = 'little')
        k.fromfile(f,)
        f = np.array(k).astype('uint8')
        print(f[0:8])

        


img2mat('./img/doge.bmp')
bin2img('./recon/doge_450_6400.bin')
