package application;

import java.io.File;

//再帰的に自身を呼び出してファイルを削除するためのクラス
class FileClass {

 public static void fileClass(File dir) {
     if(dir.exists()) {

         if(dir.isFile()) {
             if(dir.delete()) {
                 System.out.println("ファイル削除");
             }
         } else if(dir.isDirectory()) {
             File[] files = dir.listFiles();

             if(files == null) {
                 System.out.println("配下にファイルが存在しない");
             }
             //for文でファイルリスト分ループする
             for(int i=0; i<files.length; i++) {

                 File file = files[i];

                 System.out.println(file.toString());

                 //ファイルの存在確認
                 if(files[i].exists() == false) {
                     continue;
                 //ファイルの場合は再帰的に自身を呼び出して削除する
                 } else if(files[i].isFile()) {
                     //拡張子が".java"であればファイルを削除
                     if(file.getPath().endsWith(".png")) {
                         fileClass(files[i]);
                         //System.out.println("ファイル削除2");
                     }
                 }
             }
         }
     } else {
         System.out.println(dir.toString()+" ディレクトリが存在しない");
     }
 }

 /**
  * ファイル一覧取得
  * @param dir
  * @return ファイル一覧 Files[]
  */
 public static File[] getFiles(File dir) {
	 File[] files;

	 if(dir.exists()) {
		 	files = dir.listFiles();
			if( files == null ) return null;
	 }else {
		 return null;
	 }
	 return files;

 }
}
