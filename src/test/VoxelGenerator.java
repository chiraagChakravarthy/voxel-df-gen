package test;

import java.util.Arrays;
import java.util.Random;

public class VoxelGenerator {
    private static final int SCALE = 32;
    public static void main(String[] args) {
        long t1 = 0, t2 = 0;
        for (int j = 0; j < 20; j++) {
            byte[] blockMap = new byte[SCALE*SCALE*SCALE],
                    distanceMap=new byte[SCALE*SCALE*SCALE],
                    distanceMap2=new byte[SCALE*SCALE*SCALE];
            Random random = new Random();
            for (int i = 0; i < blockMap.length; i++) {
                blockMap[i] = (byte) (random.nextInt(4)==0?1:0);
            }
            long lastTime = System.currentTimeMillis();
            method1(blockMap, distanceMap);
            long now = System.currentTimeMillis();
            t1 += now-lastTime;
            lastTime = now;
            genDf(blockMap, distanceMap2);
            now = System.currentTimeMillis();
            t2 += now-lastTime;

            if(Arrays.equals(distanceMap, distanceMap2)){
                System.out.println("yay");
            } else {
                System.out.println("aww");
                System.out.println(Arrays.toString(blockMap));
                printArray(distanceMap);
                printArray(distanceMap2);
            }
        }
        System.out.println(t1);
        System.out.println(t2);
    }

    private static void printArray(byte[] b){
        StringBuilder s = new StringBuilder("[" + ((int) b[0] - Byte.MIN_VALUE));
        for (int i = 1; i < b.length; i++) {
            s.append(", ").append((int) b[i] - Byte.MIN_VALUE);
        }
        s.append("]");
        System.out.println(s);
    }

    //assume cube
    private static void method1(byte[] blockMap, byte[] distMap){
        Arrays.fill(distMap, Byte.MAX_VALUE);

        for(int x = 0; x < SCALE; x++){
            for(int y = 0; y < SCALE; y++){
                for(int z = 0; z < SCALE; z++){
                    if(getByte(blockMap, x, y, z)==1){
                        setByte(distMap, x, y, z, Byte.MIN_VALUE);
                    }
                }
            }
        }
        for (int i = 0; i < SCALE*3; i++) {
            for(int x = 0; x < SCALE; x++){
                for(int y = 0; y < SCALE; y++){
                    for(int z = 0; z < SCALE; z++){
                        byte current = getByte(distMap, x, y, z);
                        int min = min(
                                getByte(distMap, x+1, y, z),
                                getByte(distMap, x-1, y, z),
                                getByte(distMap, x, y+1, z),
                                getByte(distMap, x, y-1, z),
                                getByte(distMap, x, y, z+1),
                                getByte(distMap, x, y, z-1)
                        );
                        if(current>min){
                            setByte(distMap, x, y, z, (byte)(min+1));
                        }
                    }
                }
            }
        }
    }

    private static void genDf(byte[] blockMap, byte[] distMap){
        Arrays.fill(distMap, Byte.MAX_VALUE);
        for(int x = 0; x < SCALE; x++){
            for(int y = 0; y < SCALE; y++){
                for(int z = 0; z < SCALE; z++){
                    if(getByte(blockMap, x, y, z)==1){
                        setByte(distMap, x, y, z, Byte.MIN_VALUE);
                    }
                }
            }
        }

        int scale = 2;
        int halfScale = 1;
        while(halfScale<SCALE){
            //iterate through all the cubes in the map
            for(int x = 0; x < SCALE; x += scale){
                for(int y = 0; y < SCALE; y+=scale){
                    for(int z = 0; z < SCALE; z+=scale){

                        //iterate through all 8 halfcubes in the cube
                        for(int x1 = 0; x1 < 2; x1++){
                            for(int y1 = 0; y1 < 2; y1++){
                                for(int z1 = 0; z1 < 2; z1++){

                                    //halfcube x: the relative coordinates of the - - - block in the halfcube in the fullcube
                                    int hcX = x1*halfScale, hcY = y1*halfScale, hcZ = z1*halfScale;

                                    //iterate through all blocks in the halfcube
                                    for(int x2 = 0; x2 < halfScale; x2++){
                                        for(int y2 = 0; y2 < halfScale; y2++){
                                            for(int z2 = 0; z2 < halfScale; z2++){
                                                //find the vector from each block to the closest block in the opposite octant
                                                int dx = x1==0?(halfScale-x2):(-1-x2),
                                                        dy = y1==0?(halfScale-y2):(-1-y2),
                                                        dz = z1==0?(halfScale-z2):(-1-z2);

                                                //absolute block coordinates
                                                int bx = x2+hcX+x,
                                                        by = y2+hcY+y,
                                                        bz = z2+hcZ+z;


                                                int adx = Math.abs(dx), ady = Math.abs(dy), adz = Math.abs(dz);

                                                //get the block at all combinations of the basis vectors
                                                int min = min(
                                                        getByte(distMap, bx+dx, by, bz)+adx,
                                                        getByte(distMap, bx, by+dy, bz)+ady,
                                                        getByte(distMap, bx, by, bz+dz)+adz,
                                                        getByte(distMap, bx+dx, by+dy, bz)+adx+ady,
                                                        getByte(distMap, bx, by+dy, bz+dz)+ady+adz,
                                                        getByte(distMap, bx+dx, by, bz+dz)+adx+adz,
                                                        getByte(distMap, bx+dx, by+dy, bz+dz)+adx+adz+ady
                                                );
                                                byte current = getByte(distMap, bx, by, bz);
                                                if(current>min){
                                                    setByte(distMap, bx, by, bz, (byte) min);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            scale = scale<<1;
            halfScale = halfScale<<1;
        }
    }

    private static int min(int... in){
        int min = Integer.MAX_VALUE;
        for(int i : in){
            min = Math.min(min, i);
        }
        return min;
    }

    private static void setByte(byte[] ar, int x, int y, int z, byte val){
        if(x>=0&&x<SCALE&&y>=0&&y<SCALE&&z>=0&&z<SCALE) {
            ar[z * SCALE * SCALE + y * SCALE + x] = val;
        }
    }
    private static byte getByte(byte[] ar, int x, int y, int z){
        if(x>=0&&x<SCALE&&y>=0&&y<SCALE&&z>=0&&z<SCALE) {
            return ar[z * SCALE * SCALE + y * SCALE + x];
        }
        return Byte.MAX_VALUE;
    }
}
