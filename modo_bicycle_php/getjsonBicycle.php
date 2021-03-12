<?php
  error_reporting(E_ALL);
  ini_set('display_errors',1);

  include('db.php');

  $query = "SELECT max(bicycleID) FROM bicycle";
  $data = mysqli_query($db, $query);
  $total_rows = mysqli_fetch_array($data);

  echo $total_rows['max(bicycleID)'];
?>
