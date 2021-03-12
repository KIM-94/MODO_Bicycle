<?php

    error_reporting(E_ALL);
    ini_set('display_errors',1);

    include('dbcon2.php');

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android"); 

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
    // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.

        $userID=$_POST['userID'];
        $bicycleNumber =$_POST['bicycleNumber'];
        $rentalPlaceLatitude = $_POST['rentalPlaceLatitude'];
        $rentalPlaceLongitude = $_POST['rentalPlaceLongitude'];
        $returnPlaceLatitude =$_POST['returnPlaceLatitude'];
        $returnPlaceLongitude =$_POST['returnPlaceLongitude'];
        $rentalTime =$_POST['rentalTime'];
        $returnTime =$_POST['returnTime'];
        $useTime = $_POST['useTime'];



        if(empty($userID)){
            $errMSG = "아이디를 입력하세요.";
        }
        else if(empty($bicycleNumber)){
            $errMSG = "비밀번호를 입력하세요.";
        }
        else if(empty($rentalPlaceLatitude)){
            $errMSG = "대여장소1 입력하세요.";
        }
        else if(empty($rentalPlaceLongitude)){
            $errMSG = "대여장소2 입력하세요.";
        }
        else if(empty($returnPlaceLatitude)){
            $errMSG = "반납장소1 입력하세요.";
        }
        else if(empty($returnPlaceLongitude)){
            $errMSG = "반납장소2 입력하세요.";
        }
        else if(empty($rentalTime)){
            $errMSG = "대여시간 입력하세요.";
        }
        else if(empty($returnTime)){
            $errMSG = "반납시간 입력하세요.";
        }
        else if(empty($useTime)){
            $errMSG = "사용시간 입력하세요.";
        }

        if(!isset($errMSG)) // 이름과 나라 모두 입력이 되었다면
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
                $stmt = $con->prepare('INSERT INTO history(userID, bicycleNumber, rentalPlaceLatitude, rentalPlaceLongitude, returnPlaceLatitude, returnPlaceLongitude, rentalTime, returnTime, useTime) VALUES(:userID, :bicycleNumber, :rentalPlaceLatitude, :rentalPlaceLongitude, :returnPlaceLatitude, :returnPlaceLongitude, :rentalTime, :returnTime, :useTime)');
                $stmt->bindParam(':userID', $userID);
                $stmt->bindParam(':bicycleNumber', $bicycleNumber);
                $stmt->bindParam(':rentalPlaceLatitude', $rentalPlaceLatitude);
                $stmt->bindParam(':rentalPlaceLongitude', $rentalPlaceLongitude);
                $stmt->bindParam(':returnPlaceLatitude', $returnPlaceLatitude);
                $stmt->bindParam(':returnPlaceLongitude', $returnPlaceLongitude);
                $stmt->bindParam(':rentalTime', $rentalTime);
                $stmt->bindParam(':returnTime', $returnTime);
                $stmt->bindParam(':useTime', $useTime);



                if($stmt->execute())
                {
                    $successMSG = "새로운 사용기록을 추가했습니다.";
                    $arr["success"] = "1";
                }
                else
                {
                    $errMSG = "추가 에러";
                    $arr["success"] = "-1";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage());
                $arr["success"] = "error";
            }
        }
    }
    echo json_encode($arr,JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
?>
