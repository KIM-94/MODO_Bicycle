 <?php
  session_start();

  $db = new mysqli("localhost","input ID","input PW","input DB");
  $db->set_charset("utf8");

  function mq($sql){
    global $db;
    return $db->query($sql);
  }

  ?>
