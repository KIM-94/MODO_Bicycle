<?php
	include('db.php');
	include('password.php');

	//POST로 받아온 아이다와 비밀번호가 비었다면 알림창을 띄우고 전 페이지로 돌아갑니다.
	if($_POST['userID'] == "" || $_POST['userPassword'] == ""){
		$successMSG = "아이디나 패스워드 입력하세요.";
		$arr["success"] = "error";
	}
	else{
		//password변수에 POST로 받아온 값을 저장하고 sql문으로 POST로 받아온 아이디값을 찾습니다.
		$password = $_POST['userPassword'];
		$sql = mq("select * from person where userID='".$_POST['userID']."'");
		$member = $sql->fetch_array();
		$hash_pw = $member['userPassword']; //$hash_pw에 POSt로 받아온 아이디열의 비밀번호를 저장합니다.

		if(password_verify($password, $hash_pw)) {
		//만약 password변수와 hash_pw변수가 같다면 세션값을 저장하고 알림창을 띄운후 main.php파일로 넘어갑니다.
			$_SESSION['userID'] = $member["userID"];
			$_SESSION['userPassword'] = $member["userPassword"];

			$arr["success"] = "1";
		} else{
			$arr["success"] = "-1";
		}
	}
	echo json_encode($arr,JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE); 
?>
