var Review = {};
var RowTemplate;

Review.row = function(patch) {
  var result = $($.jqote(RowTemplate, patch)).data(patch);
  result.find(".subject").click(function (){
      result.find(".diff").toggle();
      return false;
  });
  return result;
};

Review.getJSON = function(url, func) {
  $.getJSON(url, func);
};

Review.checkMail = function () {
  Review.getJSON("/api/list", function(data) {
    $.each(data, function(index, patch) { Review.prependRow(patch);});
  });
};

Review.addPatch = function(patch) {
  Review.prependRow(patch);
};

Review.patches = [];

Review.me = function() {
  return $("#name").val();
};

Review.approve = function(id, who) {
  $.post("/api/approve", {"id":id, "who":who});
};

Review.disapprove = function(id, who) {
  $.post("/api/disapprove", {"id":id, "who":who});
};

Review.updateAcceptors = function(id, acceptors) {
  var patchDiv = Review.patchElement(id);
  if (patchDiv.length > 0) {
    patchDiv.data().acceptors = acceptors;
    Review.updateAcceptorsText(patchDiv);
    Review.updatePatchStyle(patchDiv);
    Review.updateAcceptLink(patchDiv);
  }
};

Review.updateAcceptLink = function(patchDiv) {
  var acceptLink = patchDiv.find(".acceptLink");
  var patch = patchDiv.data();
  acceptLink.unbind("click");
  if (Review.acceptedByMe(patch.acceptors)) {
    acceptLink.text("Disapprove").click(function() {
      if (Review.me() == "") {
        alert("Enter name first on bottom right");
      } else {
        Review.disapprove(patch.id, Review.me());
      }
      return false;
    });
  } else {
    acceptLink.text("Approve").click(function() {
      if (Review.me() == "") {
        alert("Enter name first on bottom right");
      } else {
        Review.approve(patch.id, Review.me());
      }
      return false;
    });
  }
};


Review.updatePatchStyle = function(patchDiv) {
  var accepted = patchDiv.data().acceptors.length > 0;
  patchDiv.toggleClass("accepted", accepted);
  patchDiv.toggleClass("notAccepted", !accepted);
};

Review.updateAcceptorsText = function(patchDiv) {
  var acceptors = patchDiv.data().acceptors;
  var newAcceptors = acceptors.length > 0 ? "(" + acceptors.join(", ") + ")" : "";
  patchDiv.find(".acceptedBy").text(newAcceptors);
};

Review.acceptedByMe = function(acceptors) {
  return $.inArray(Review.me(), acceptors) != -1;
};

Review.patchElement = function(id) {
  return $("#patch"+id);
};

Review.prependRow = function(patch) {
  $("#patches").prepend(Review.row(patch));
  Review.patches.push(patch);
  Review.updateAcceptors(patch.id, patch.acceptors);
};

Review.updateAllAcceptors = function() {
  $.each(Review.patches, function(index, patch) {
    Review.updateAcceptors(patch.id, patch.acceptors);
  });
};

$(function () {
  RowTemplate = $.jqotec("#rowTemplate");
  Review.checkMail();
  $("#name").val($.cookie("name"));
  var existingTimeout = null;
  $("#name").keyup(function() {
    $.cookie("name", this.value);
    clearTimeout(existingTimeout);
    existingTimeout = setTimeout(Review.updateAllAcceptors, 1000);
  });
});
