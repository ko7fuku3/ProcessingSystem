package processingSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;



public class processingSystem {

  //配列のインデックスの定数化
  final static int INDEX_FIRST = 0;
  final static int INDEX_SECOND = 1;
  final static int INDEX_THIRD = 2;

  //正午を定義
  final static int NOON = 720;
  //13時を定義
  final static int AFTERNOON = 780;
  //定時を定義
  final static int ORDINARY = 1080;

  //勤怠管理配列を定義
  private static ArrayList<LinkedHashMap<String, Object>> processingtLists = new ArrayList<LinkedHashMap<String, Object>>();
  private static LinkedHashMap<String, Object> processingList;

  //勤務時間計算配列を定義
  private static ArrayList<LinkedHashMap<String, Object>> workTimeLists = new ArrayList<LinkedHashMap<String, Object>>();
  private static LinkedHashMap<String, Object> workTimeList;

  //集計結果配列を定義
  private static ArrayList<LinkedHashMap<String, Object>> totalList = new ArrayList<LinkedHashMap<String, Object>>();




	public static void main(String[] args) {

		//引数チェック
		if(args.length <= 0) {

			System.out.println("ファイルを指定してください！");
			System.exit(0);

		}

		try {
		//入力用ファイルパスを変数inputFilePassに代入
		String inputFilePass = args[0];

		//出力用ファイルパスを変数outputFilePassに代入
		String outputFilePass = args[1];

		//ファイル読込関数
	    fileInput(inputFilePass);

		//就業時間・残業時間計算関数
		calcWorkTime(processingtLists);

		//集計関数
		aggreGate(workTimeLists, workTimeList);

		//CSVファイル出力関数呼び出し
		outputFile(outputFilePass, totalList);

		} catch(FileNotFoundException e) {
			System.out.println("指定したファイルが見つかりません");
			System.exit(1);
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			System.exit(1);
		} catch(ParseException e){
			System.out.println("時間のフォーマットエラー");
			System.exit(1);
		} catch(NumberFormatException e) {
			System.out.println("入力値が不正です");
			System.exit(1);
		} catch(NullPointerException e) {
			System.out.println("入力値がありません");
			System.exit(1);
		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println("入力形式が不正です");
			System.exit(1);
		}

	}

	/**
	*ファイル読込関数
	*
	*@param filePass CSVファイル名
	*@return processingtLists 勤怠管理配列
	*/
	public static ArrayList fileInput(String filePass) {

		try {

		  //FileReaderオブジェクトをBufferedReaderオブジェクトでラッピングして、
		  //CSVファイルをまとめて読み込める様にする。
          BufferedReader br = new BufferedReader(new FileReader(filePass));

		  //CSVファイルの1行を代入する変数lineを定義
	  	  String line;
	  	  int i = 0;
			//CSVファイルを1行ずつ読み込む処理
			while((line = br.readLine()) != null) {

				//ダブルクォーテーションを除去
				line = line.replace("\"","");

				//先頭の空白を除去
				line = line.trim();

				//1行目は読み飛ばし
				if (i == 0) {
					i++;
				}
				else {
					//就業時間レコードを配列employRecordに格納
					String[] employRecord = line.split(",");

					//就業時間レコードを配列processingListに格納
					processingList = new LinkedHashMap<String, Object>();
					processingList.put("name", employRecord[INDEX_FIRST]);
					processingList.put("startingHours", employRecord[INDEX_SECOND]);
					processingList.put("closingTime", employRecord[INDEX_THIRD]);
					processingtLists.add(processingList);
				}
			}

			br.close();

		 	} catch(IOException e) {

				System.out.println("入出力エラーです。");

			}

		  return processingtLists;
	}

	/**
	*勤務時間計算関数
	*
	*@param arrayList 勤怠管理配列
	*@return workTimeList 勤務時間配列
	*/
	private static ArrayList calcWorkTime(ArrayList arraylist) {

		//勤務時間計算配列を定義
		LinkedHashMap<String, Object> calcWorkTimeList = new LinkedHashMap<String, Object>();

		//始業時間配列を定義
		String[] startTimeArr = null;

		//終業時間配列を定義
		String[] endTimeArr = null;

		for (int i=0; i<processingtLists.size(); i++) {

			calcWorkTimeList = processingtLists.get(i);

			//始業時間をhh、mmに分割し配列startTimeArrに格納
			startTimeArr = calcWorkTimeList.get("startingHours").toString().split(":");

			//始業時間をint型に変換
			int startHour = Integer.parseInt(startTimeArr[INDEX_FIRST]);
			int startMin = Integer.parseInt(startTimeArr[INDEX_SECOND]);

			//始業時間のhh:mmを分単位に変換
			int startTime = (startHour * 60) + startMin;

			//終業時間をhh、mmに分割し配列endSTimeArrに格納
			endTimeArr = calcWorkTimeList.get("closingTime").toString().split(":");

			//終業時間をint型に変換
			int endHour = Integer.parseInt(endTimeArr[INDEX_FIRST]);
			int endMin = Integer.parseInt(endTimeArr[INDEX_SECOND]);

			//終業時間のhh:mmを分単位に変換
			int endTime = (endHour * 60) + endMin;

			/*就業時間の計算*/
			//午前時間を計算
			int amWorkTime = NOON - startTime;

			//午前時間がマイナスならば０を代入
			if (amWorkTime < 0) {
				amWorkTime = 0;
			}

			//午後時間を計算
			int pmWorkTime = endTime - AFTERNOON;

			//午後時間がマイナスならば0を代入
			if (pmWorkTime < 0) {
				pmWorkTime = 0;
			}

			/*残業時間の計算*/
			//残業時間を定義
			int overTime = 0;

			//18時以降は残業
			if (endTime > ORDINARY) {

				overTime = endTime - ORDINARY;
			}

			//就業時間を計算
			int employmentTime = amWorkTime + pmWorkTime - overTime;

			//勤務時間計算配列に格納
			workTimeList = new LinkedHashMap<String, Object>();
			workTimeList.put("name", calcWorkTimeList.get("name"));
			workTimeList.put("employmentTime", employmentTime);
			workTimeList.put("overTime", overTime);
			workTimeLists.add(workTimeList);
		}

		return workTimeLists;
	}

	/*
	*集計関数
	*
	*@param worktimelists,worktimelist 勤務計算配列
	*@return totalList 勤務時間集計配列
	*/
	private static ArrayList aggreGate(ArrayList worktimelists, LinkedHashMap worktimelist) {

		//集計結果の初期化
		//集計結果を一時的に格納するresultsMap配列を定義する
		LinkedHashMap<Object, LinkedHashMap<String, Object>> resultMap = new LinkedHashMap<Object, LinkedHashMap<String, Object>>();

		//集計配列を定義
		LinkedHashMap<String, Object> aggreGateList = new LinkedHashMap<String, Object>();

		ArrayList <LinkedHashMap<String, Object>> a = new ArrayList<LinkedHashMap<String, Object>>();

		//勤務時間配列の要素分だけ繰り返す
		for (int i=0; i<workTimeLists.size(); i++) {

			aggreGateList = workTimeLists.get(i);

			//勤務時間配列のnameを取得する
			Object name = aggreGateList.get("name");

			if (!resultMap.containsKey(name)) {

				//集計結果の初期値を生成する
				LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
				result.put("name", name);
				result.put("total_employmentTime", 0);
				result.put ("total_overTime", 0);

				//生成した初期値をキーnameに関連付けてマップに格納する
				resultMap.put(name, result);
			}
		}

		//勤務時間を集計
		for (int i=0; i<workTimeLists.size(); i++) {

			//個人レコードを集計配列に格納
			aggreGateList = workTimeLists.get(i);

			//勤務時間の氏名を取得する
			Object name = aggreGateList.get("name");

			//キーnameに関連付く集計結果をresultMapから取得する
			LinkedHashMap<String, Object> aggreGateMapList = resultMap.get(name);

			//勤怠データのtotal_employmentTime（就業時間）を集計結果に足し合わせる
			int p_total_employmentTime = (Integer) aggreGateList.get("employmentTime");
			int r_total_employmentTime = (Integer) aggreGateMapList.get("total_employmentTime");
			aggreGateMapList.put("total_employmentTime", p_total_employmentTime + r_total_employmentTime);

			//勤怠データのtotal_overTime（残業時間）を集計結果に足し合わせる
			int p_total_overTime = (Integer) aggreGateList.get("overTime");
			int r_total_overTime = (Integer) aggreGateMapList.get("total_overTime");
			aggreGateMapList.put("total_overTime", p_total_overTime + r_total_overTime);
		}

		//集計結果をリストに移し替え
		ArrayList<LinkedHashMap<String, Object>> results = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> result;

		//一時マップの全てのキーとデータについて繰り返す
		for (Object name : resultMap.keySet()) {

			//データをリストに追加する
			result = resultMap.get(name);
			results.add(result);
		}

		//集計結果をhh:mmに変換し格納
		for (int i=0; i<results.size(); i++) {
			result = results.get(i);
			//集計結果配列を定義
			LinkedHashMap<String, Object> totalList2 = new LinkedHashMap<String, Object>();

			totalList2.put("name",result.get("name"));
			totalList2.put("total_employmentTime",(Integer)(result.get("total_employmentTime"))/60 + ":" + String.format("%02d",(Integer)(result.get("total_employmentTime"))%60));
			totalList2.put("total_overTime",(Integer)(result.get("total_overTime"))/60 + ":" + String.format("%02d",(Integer)(result.get("total_overTime"))%60));
			totalList.add(totalList2);
		}

		return totalList;
	}

	/**
	 * ファイル出力関数
	 *
	 */
	private static void outputFile(String outputFilePass, ArrayList arr) throws FileNotFoundException, IOException,ParseException {

		//出力先ファイルを作成する
		FileWriter fw = new FileWriter(outputFilePass, false);
		PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
		String printLine;

		//項目入力
		printLine = "氏名,就業時間集計,残業時間集計";
		pw.print(printLine);
		pw.println();

		//書き込み処理
		for (int i=0; i<totalList.size(); i++) {

			printLine = totalList.get(i).get("name").toString();
			printLine = printLine + "," + totalList.get(i).get("total_employmentTime").toString();
			printLine = printLine + "," + totalList.get(i).get("total_overTime").toString();
			pw.print(printLine);
			pw.println();
		}

		pw.close();
	}
}
