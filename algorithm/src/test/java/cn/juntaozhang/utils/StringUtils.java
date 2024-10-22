package cn.juntaozhang.utils;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author juntzhang
 */
public class StringUtils {

    public static int[][] file2arr(Path p) {
        try {
            return str2arr(IOUtils.toString(Files.newInputStream(p.toFile().toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[][] str2arr2(String s) {
        s = s.replace("[[","").replace("]]","");
        return Arrays.stream(s.split("],\\[")).map(l -> {
            String[] arr = l.split(",");
            int[] arr2 = new int[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arr2[i] = Integer.parseInt(arr[i]);
            }
            return arr2;
        }).collect(Collectors.toList()).toArray(new int[0][0]);
    }

    public static int[][] str2arr(String s) {
        return Arrays.stream(s.split("],\\[")).map(l -> {
            String[] arr = l.split(",");
            return new int[]{
                    Integer.parseInt(arr[0]),
                    Integer.parseInt(arr[1])
            };
        }).collect(Collectors.toList()).toArray(new int[0][0]);
    }

    public static void print(boolean[][] mat) {
        System.out.println();
        for (int i = 0; i < mat.length; i++) {
            if (i == 0) {
                System.out.print("    \t");
                for (int j = 0; j < mat[0].length; j++) {
                    System.out.printf("% 2d\t", j);
                }
                System.out.println();
            }
            System.out.printf("% 3d:\t", i);
            for (int j = 0; j < mat[0].length; j++) {
                System.out.printf("% 2d\t", mat[i][j] ? 1 : 0);
            }
            System.out.println();
        }
    }

    public static void print(String mat) {
        for (int i = 0; i < mat.length(); i++) {
            System.out.printf("%2c\t", mat.charAt(i));
        }
        System.out.println();
    }
    public static void print(int[] mat) {
        for (int i = 0; i < mat.length; i++) {
            System.out.printf("%2d\t", i);
        }
        System.out.println();
        for (int i = 0; i < mat.length; i++) {
            System.out.printf("%2d\t", mat[i]);
        }
        System.out.println();
    }
    public static void print2(int[] mat) {
        for (int i = 0; i < mat.length; i++) {
            System.out.printf("%2d\t", mat[i]);
        }
        System.out.println();
    }

    public static void print(int[][] mat) {
        System.out.println();
        for (int i = 0; i < mat.length; i++) {
            if (i == 0) {
                System.out.print("    \t");
                for (int j = 0; j < mat[0].length; j++) {
                    System.out.printf("% 2d\t", j);
                }
                System.out.println();
            }
            System.out.printf("% 3d:\t", i);
            for (int j = 0; j < mat[0].length; j++) {
                System.out.printf("% 2d\t", mat[i][j]);
            }
            System.out.println();
        }
    }

    public static int[] readArray(String file) {
        try {
            String s = IOUtils.toString(new FileInputStream(file));
            return Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
