from bit_funcs import *
import bitarray as bt

# Converts image to many matrices each representing a 
def channel2mat(fn,bpp = 8):
    x = img2bin_repr(fn)
    r = np.zeros((x.shape[0],x.shape[1],bpp),dtype = 'uint8')
    for i,row in enumerate(x):
            for j,val in enumerate(row):
                    r[i,j,:] = np.array([int(d) for d in list(val)])
    n,m = r.shape[0],r.shape[1]
    for i in range(bpp):
        bits = list(r[:,:,i].reshape(n*m))
        with open("./bin_out/CHANNEL-%d-%s_%d_%d.bin"%((i+1),fn.split('.')[1].split('/')[-1],n,m),'wb') as f:
            bt.bitarray(bits).tofile(f)

def submat2channel(fn,bpp = 8):
    m = int(fn.split('_')[-1].split(".")[0])
    n = int(fn.split('_')[-2])
    f= open(fn,'rb')
    k = bt.bitarray(endian = 'big')
    k.fromfile(f)
    channel = np.array(k).astype('uint8')
    channel = channel[0:n*m]
    channel = channel.reshape((n,m))
    return channel
def mat2channel(basefn,bpp=8):
    n = int(basefn.split('_')[-2])
    m = int(basefn.split('_')[-1].split(".")[0])
    r = np.zeros((n,m,bpp),dtype="uint8")
    for i in range(bpp):
        r[:,:,i] = submat2channel("./bin_in/CHANNEL-%d-"%(i+1) + basefn)
    bins = r.astype('str')
    bin_repr = np.array([["".join(list(col)) for col in row] for row in bins])
    im = bin_repr2img(bin_repr)
    return im
    
    
if __name__ == "__main__":
    fn_list = []
    for fn in os.listdir("bin_in"):
        fn_list.append(fn.split("-")[-1])
    fn_list = list(set(fn_list))
    print(fn_list)

    for fn in fn_list:
        im = mat2channel(fn)
        im.save(open("./img_out/"+fn.split(".")[0] + ".bmp","wb"))
        # channel2mat("./img_in/"+fn)

    # fn = "./img_in/doge.bmp"
    # channel2mat(fn)

    # im = mat2channel("doge_450_800.bin")
    # im.save(open("./xordecompose_1iters_k450.bmp","wb"))