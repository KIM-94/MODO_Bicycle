
<?php
  error_reporting(E_ALL);
  ini_set('display_errors',1);

  include('dbcon2.php');

  $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

  if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ) {
  // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.
    $bicycleNumber =$_POST['bicycleNumber'];
    $Check = $_POST['check'];
    $rentalPlaceLatitude =$_POST['rentalPlaceLatitude'];
    $rentalPlaceLongtitude =$_POST['rentalPlaceLongtitude'];

    if(!isset($errMSG)) // 이름과 나라 모두 입력이 되었다면
    {
      if($Check == "rental"){ 

        try{
            // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
            $sql="UPDATE bicycle SET bicycleState = 'unavailable' WHERE bicycleNumber = '$bicycleNumber'";
            $stmt = $con->prepare($sql);
            $stmt->execute();
        } catch(PDOException $e) {
            die("Database error: " . $e->getMessage());
        }
      }

      else if($Check == "return"){
        try{
            // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
            $sql="UPDATE bicycle SET bicycleState = 'available' WHERE bicycleNumber = '$bicycleNumber'";
            $stmt = $con->prepare($sql);
            $stmt->execute();

            // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
            $sql="UPDATE bicycle SET bicycleState = 'available', rentalPlaceLatitude = '$rentalPlaceLatitude', rentalPlaceLongtitude = '$rentalPlaceLongtitude' WHERE bicycleNumber = '$bicycleNumber'";
            $stmt = $con->prepare($sql);
            $stmt->execute();
        } catch(PDOException $e) {
            die("Database error: " . $e->getMessage());
        }
      }
    }
  }
?>
