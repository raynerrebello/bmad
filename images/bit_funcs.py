from PIL import Image
import numpy as np
import pprint as pp
from palette import web_palette
from matplotlib.pyplot import imsave
import bitarray as bt
import os
pad = lambda x :'0'*(8-len(x)) + x if len(x) < 8 else x

# Returns a 2-d np-array of length 8 strings for every pixel
def img2bin_repr(fn,bpp = 8):
        im = Image.open(fn)
        arr = np.array(im)
        if bpp == 8:
                bin_repr = [[pad(bin(pixel).lstrip('0b')) for pixel in row] for row in arr]
        else:
                bin_repr = [[''.join([pad(bin(channel).lstrip('0b')) for channel in pixel]) 
                                                        for pixel in row] for row in arr]

        return np.array(bin_repr)

# Returns an PIL Image object from an array of 8-bit binary strings
def bin_repr2img(bin_repr,bpp = 8,palette = web_palette):
        if bpp ==8:
                arr = np.array([[int(str(pixel), 2) for pixel in row] for row in bin_repr],dtype = 'uint8')
                im = Image.fromarray(arr,mode='P')
                im.putpalette(palette)
        if bpp ==24:
                arr = np.array([[ [int(str(pixel[i:i+8]),2) for i in range(0,24,8)]for pixel in row] for row in bin_repr],dtype = 'uint8')
                im = Image.fromarray(arr,mode='RGB')
        im.show()
        return im

# Write image to a binary matrix for BMaD
def img2mat(fn,bpp=8):
        x = img2bin_repr(fn,bpp)
        print(x[0,0])
        r = np.zeros((x.shape[0],x.shape[1],bpp),dtype = 'uint8')
        for i,row in enumerate(x):
                for j,val in enumerate(row):
                        r[i,j,:] = np.array([int(d) for d in list(val)])
        r = r.reshape(r.shape[0], bpp*r.shape[1])
        n,m = r.shape
        r = r.reshape(r.shape[0]*r.shape[1])
        r = list(r)
        assert(len(r)==(x.shape[0]*x.shape[1]*bpp))
        with open("./bin_out/%s_%d_%d.bin"%(fn.split('.')[1].split('/')[-1],n,m),'wb') as f:
                bt.bitarray(r).tofile(f)
                
# Read in a binary file and interpret it as an image.
def bin2img(fn,m,bpp = 8):
        f= open(fn,'rb')
        k = bt.bitarray(endian = 'big')
        k.fromfile(f)
        f = np.array(k).astype('uint8')
        bins = np.reshape(f,(-1,int(m/8),8)).astype('str')
        bin_repr = np.array([["".join(list(col)) for col in row] for row in bins])
        im = bin_repr2img(bin_repr,bpp)
        return im

if __name__ == "__main__":

        # fn = "./bin_in/ColoredGrids3_462_3760.bin"

        # f= open(fn,'rb')
        # k = bt.bitarray(endian = 'little')
        # k.fromfile(f)
        # k = np.array(k).astype('uint8')


        # fn = "./bin_out/ColoredGrids3_462_3760.bin"

        # f= open(fn,'rb')
        # l = bt.bitarray(endian = 'big')
        # l.fromfile(f)
        # l = np.array(l).astype('uint8')

        # diff = sum(l-k)


        # print("hi")


        for fn in os.listdir("img_in"):
                img2mat("./img_in/"+fn,24)

        # for fn in os.listdir("bin_in"):
        #         try:
        #                 print(int(fn.split('_')[-1].split(".")[0]))
        #                 im = bin2img("./bin_in/"+fn,int(fn.split('_')[-1].split(".")[0]))
        #                 im.save(open("./img_out/" + fn.split('_')[0] + "_RECON.bmp" ,"wb"))
        #         except Exception:
        #                 print(fn)



        #im = bin2img("bin_in\\" + fn,int(fn.split('_')[-1].split(".")[0]))

        # # r = img2bin_repr("./img/reddot.bmp")
        # # b = bin_repr2img(r)
        # f= open("recon\doge_450_6400.bin",'rb')
        # k = bt.bitarray(endian = 'little')
        # k.fromfile(f)
        # g = np.array(k)
        # print(np.sum(g)/len(g))
