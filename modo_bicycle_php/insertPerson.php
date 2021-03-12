<?php

    error_reporting(E_ALL);
    ini_set('display_errors',1);

    include('dbcon2.php');

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
    // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.

        $userID=$_POST['userID'];
        $userPassword=password_hash($_POST['userPassword'], PASSWORD_DEFAULT);
        $userName=$_POST['userName'];
        $userBirthday=$_POST['userBirthday'];
        $userEmail=$_POST['userEmail'];

        if(empty($userID)){
            $errMSG = "아이디를 입력하세요.";
        }
        else if(empty($userPassword)){
            $errMSG = "비밀번호를 입력하세요.";
        }
        else if(empty($userName)){
            $errMSG = "이름을 입력하세요.";
        }
        else if(empty($userBirthday)){
            $errMSG = "생년월일을 입력하세요.";
        }
        else if(empty($userEmail)){
            $errMSG = "이메일을 입력하세요.";
        }

        if(!isset($errMSG)) // 이름과 나라 모두 입력이 되었다면
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
                $stmt = $con->prepare('INSERT INTO person(userID, userPassword, userName, userBirthday, userEmail) VALUES(:userID, :userPassword, :userName, :userBirthday, :userEmail)');
                $stmt->bindParam(':userID', $userID);
                $stmt->bindParam(':userPassword', $userPassword);
                $stmt->bindParam(':userName', $userName);
                $stmt->bindParam(':userBirthday', $userBirthday);
                $stmt->bindParam(':userEmail', $userEmail);


                if($stmt->execute())
                {
                    $successMSG = "새로운 사용자를 추가했습니다.";
                    $arr["success"] = "1";
                }
                else
                {
                    $errMSG = "사용자 추가 에러";
                    $arr["success"] = "-1";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage());
            }
        }
    }
    echo json_encode($arr,JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
?>
