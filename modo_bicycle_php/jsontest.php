<?php

//echo file_get_contents("php://input")."<br/>";
$chatlist_jsonlist = file_get_contents("php://input");
$data = json_decode($chatlist_jsonlist)->{"webnautes"};
echo $data->webnautes;
//echo json_decode(file_get_contents("php://input"))->webnautes;
//$chatlist_jsonlist = json_decode(file_get_contents("php://input"))->{"webnautes"};
//$chatlist_jsonlist = json_decode($_POST['webnautes'],true);     //POST로 받은 값을 json형식으로 decode

for($i = 0 ; $i < count($chatlist_jsonlist) ; $i++){

  //JSONArray에서 [$i] 번째 행의 JSONObject [' '] 항목의 값을 가져옴
  $member_nm = "'".$chatlist_jsonlist[$i]['bicycleNumber']."'";
  $chatmsg = "'".$chatlist_jsonlist[$i]['macAddress']."'";
  $msg_timestamp = "'".$chatlist_jsonlist[$i]['rentalPlaceLatitude']."'";

  //테스트 출력
  echo $member_nm."/".$chatmsg."/".$msg_timestamp;
}
?>
