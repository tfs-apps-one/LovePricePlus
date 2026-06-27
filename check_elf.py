import sys

def check_alignment(file_path):
    with open(file_path, "rb") as f:
        magic = f.read(4)
        if magic != b"\x7fELF":
            print(f"{file_path} is not an ELF file")
            return
            
        is_64 = f.read(1) == b"\x02"
        f.seek(0x1c if not is_64 else 0x20)
        phoff = int.from_bytes(f.read(4 if not is_64 else 8), byteorder="little")
        
        f.seek(0x2a if not is_64 else 0x36)
        phentsize = int.from_bytes(f.read(2), byteorder="little")
        phnum = int.from_bytes(f.read(2), byteorder="little")
        
        aligned_to_16k = True
        for i in range(phnum):
            f.seek(phoff + i * phentsize)
            p_type = int.from_bytes(f.read(4), byteorder="little")
            if p_type == 1: # PT_LOAD
                f.seek(phoff + i * phentsize + (0x1c if not is_64 else 0x30))
                p_align = int.from_bytes(f.read(4 if not is_64 else 8), byteorder="little")
                print(f"LOAD segment {i} alignment: {p_align} (0x{p_align:x})")
                if p_align < 16384:
                    aligned_to_16k = False
                    
        if aligned_to_16k:
            print(f"✅ {file_path} is 16KB aligned")
        else:
            print(f"❌ {file_path} is NOT 16KB aligned")

if __name__ == "__main__":
    check_alignment(sys.argv[1])
