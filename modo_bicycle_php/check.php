<?php
	include('db.php');

	if($_POST['userID']==""){
			$successMSG = "아이디를 입력하세요.";
			$arr["success"] = "0";
	}	else{
			$uid = $_POST['userID'];

			$sql = mq("select * from person where userID='".$uid."'");
			$member = $sql->fetch_array();

			if($member==0) {
				$successMSG = "사용가능한 아이디입니다.";
				$arr["success"] = "1";
			}
			else {
				$successMSG = "중복된아이디입니다.";
				$arr["success"] = "-1";
			}
	}
	echo json_encode($arr,JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
?> 
