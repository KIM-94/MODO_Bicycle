<?php
//	include('dbcon2.php');
	include('db.php');

	if($_POST['macAddress']=="") {
			$successMSG = "MAC주소를 입력하세요.";
	}	else {
			$uid = $_POST['macAddress'];

			$sql = mq("select * from bicycle where macAddress='".$uid."'");
			$member = $sql->fetch_array();

			if($member==0) {
				$successMSG = "사용가능한 MAC주소입니다.";
			}
			else {
				$successMSG = "중복된 MAC주소입니다.";
			}
	}
?>

<?php
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;
?> 
