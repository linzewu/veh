<html>
    <head>
    <link rel="stylesheet" href="js/edge/webix.css" type="text/css"> 
    <script src="js/edge/webix.js" type="text/javascript"></script>  
    </head>
    <body>
        <script type="text/javascript" charset="utf-8">
       var data = webix.ajax("data.json");
       console.log(data);
        webix.ui({
        	  rows:[
        	    { type:"header", template:"My App!" },
        	    { cols:[
        	      { view:"tree", gravity:0.4, select:true },
        	      { view:"resizer" },
        	      { view:"datatable", autoConfig:true }
        	    ]}
        	  ]
        	});
        </script>
    </body>
</html>