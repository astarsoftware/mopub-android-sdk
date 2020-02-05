describe('MRAID', function() {
  var MRAID, BRIDGE;

  beforeEach(function() {
    MRAID = mraid;
    BRIDGE = mraidbridge;
  });

  describe("BRIDGE", function() {
    var lastCall = function() {
      return BRIDGE.nativeCallQueue.pop();
    };

    beforeEach(function() {
      BRIDGE.nativeSDKFiredReady = true;
      BRIDGE.nativeCallInFlight = true;
    });
      
    it("correctly composes a URL", function() {
      BRIDGE.executeNativeCall(['commandname', 'arg1', true, 'arg2', false]);
      expect(lastCall()).toEqual('mraid://commandname?arg1=true&arg2=false');
    });
  });
  
  describe('.removeEventListener', function() {
    var funcSpy;
    var errorSpy;
    
    beforeEach(function () {
      funcSpy = jasmine.createSpy();
      MRAID.addEventListener(MRAID.EVENTS.VIEWABLECHANGE, funcSpy);
      
      errorSpy = jasmine.createSpy();
      MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
    });
    
    afterEach(function () {
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE);
      MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
    });
    
    it('should remove the listener passed in', function() {
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, funcSpy);
      BRIDGE.fireChangeEvent({viewable: true});
      expect(funcSpy).not.toHaveBeenCalled();
    });
    
    it("should remove the only listener when we don't specify a listener", function() {
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE);
      BRIDGE.fireChangeEvent({viewable: true});
      expect(funcSpy).not.toHaveBeenCalled();
    });
    
    it('should not do anything when we remove a listener that was never added', function(){
      var bogusFunc = function (e) {var huh = "idontknow";};
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, bogusFunc);
      BRIDGE.fireChangeEvent({viewable: true});
      expect(funcSpy).toHaveBeenCalled();
    });
    
    it('should produce an error when no recognizable event is passed in', function() {
      MRAID.removeEventListener('hotdogsdfasdfasdfsdfsdfsdfsfsf', funcSpy);
      expect(errorSpy).toHaveBeenCalled();
    });

    it('should produce an error when passing in a listener that does not belong to the event', function() {
      var bogusFunc = function(e) {var huh = "idontknow";};
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, bogusFunc);
      expect(errorSpy).toHaveBeenCalled();
    });

    it('should produce an error when nothing is passed in', function() {
      MRAID.removeEventListener();
      expect(errorSpy).toHaveBeenCalled();
    }); 

    describe('when there are multiple event listeners for one event', function() {
      var spy1, spy2, spy3, spyFn1, spyFn2, spyFn3;

      beforeEach(function() {
        spy1 = false;
        spy2 = false;
        spy3 = false;

        spyFn1 = function() { spy1 = true; };
        spyFn2 = function() { spy2 = true; };
        spyFn3 = function() { spy3 = true; };

        MRAID.addEventListener(MRAID.EVENTS.VIEWABLECHANGE, spyFn1);
        MRAID.addEventListener(MRAID.EVENTS.VIEWABLECHANGE, spyFn2);
        MRAID.addEventListener(MRAID.EVENTS.VIEWABLECHANGE, spyFn3);
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE);
      });
      
      it('should remove all listeners when no listener is passed in for the event', function() {
        MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE);
        BRIDGE.notifyViewableChangeEvent(true);
        expect(spy1).not.toEqual(true);
        expect(spy2).not.toEqual(true);
        expect(spy3).not.toEqual(true);
      });
      
      it('should not do anything when we remove a listener that was never added', function(){
        var bogusFunc = function (e) {var huh = "idontknow";};
        MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, bogusFunc);
        BRIDGE.notifyViewableChangeEvent(true);
        expect(spy1).toEqual(true);
        expect(spy2).toEqual(true);
        expect(spy3).toEqual(true);
      });
      
      it('should only remove the listener passed in', function() {
        // x and z should change, but y should not since we removed the function that modifies y.
        MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, spyFn2);
        BRIDGE.notifyViewableChangeEvent(true);
        expect(spy1).toEqual(true);
        expect(spy2).not.toEqual(true);
        expect(spy3).toEqual(true);
      });
      
      it('should not affect any listeners when nothing is passed in', function() {
        MRAID.removeEventListener();
        BRIDGE.notifyViewableChangeEvent(true);
        expect(spy1).toEqual(true);
        expect(spy2).toEqual(true);
        expect(spy3).toEqual(true); 
      });

      it("should remove all listeners when no specific listener is provided", function() {
        MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE);
        BRIDGE.notifyViewableChangeEvent(true);
        expect(spy1).not.toEqual(true);
        expect(spy2).not.toEqual(true);
        expect(spy3).not.toEqual(true);
      });
    });
  });

  describe("when you add an event listener", function() {
    var version;

    beforeEach(function() {
      version = false;

      var eventListener = function() {
        version = this.getVersion();
      };

      MRAID.addEventListener(MRAID.EVENTS.READY, eventListener);
    });

    afterEach(function() {
      MRAID.addEventListener(MRAID.EVENTS.READY);
    });

    it("executes in the context of mraid", function() {
      BRIDGE.notifyReadyEvent();
      expect(version).toEqual("2.0");
    });
  });

  describe('.close', function() {
    describe("when calling close", function() {
      beforeEach(function() {
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.close();
      });

      it("executes native call", function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith(['close']);
      });
    });
  });

  describe('.playVideo', function() {
    describe('when called when the ad is not viewable', function() {
      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: false});
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
        MRAID.playVideo("http://early.bird");
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('does not execute native call and an error is fired', function() {
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'playVideo');
      });
    });

    describe('when called incorrectly and the ad is viewable', function() {
      var errorSpy;

      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: true});
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
        MRAID.playVideo(); // should've used a URL
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('does not execute native call and an error is fired', function() {
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'playVideo')
      });
    });

    describe('when called correctly and the ad is viewable', function() {
      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: true});
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.playVideo('http://www.youtube.com/watch?v=nGYVjRrBhWo');
      });

      it('tells the SDK to play a video with the url', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'playVideo',
          'uri',
          'http://www.youtube.com/watch?v=nGYVjRrBhWo'
        ]);
      });
    });
  });

  describe('.storePicture', function() {
    describe('when called when the ad is not viewable', function() {
      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: false});
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
        MRAID.storePicture("http://dummyimage.com/600x400/000/fff");
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('does not execute native call and an error is fired', function() {
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'storePicture')
      });
    });

    describe('when called incorrectly and the ad is viewable', function() {
      var errorSpy;

      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: true});
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
        MRAID.storePicture() // shoul've used a URI
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('does not execute native call and an error is fired', function() {
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'storePicture')
      });
    })

    describe('when called correctly and the ad is viewable', function() {
      beforeEach(function() {
        BRIDGE.fireChangeEvent({viewable: true});
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.storePicture("http://dummyimage.com/600x400/000/fff");
      });

      it('tells the SDK to store a picture with the url', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
         'storePicture',
          'uri',
          'http://dummyimage.com/600x400/000/fff'
        ]);
      });
    });

  });


  describe('.expand', function() {
    describe('when called in default state', function() {
      beforeEach(function() {
        spyOn(MRAID, 'getState').andReturn('default');
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.expand();
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'expand', 'shouldUseCustomClose', false
        ]);
      });
    });

    describe('when called in resized state', function() {
      beforeEach(function() {
        spyOn(MRAID, 'getState').andReturn(MRAID.STATES.RESIZED);
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.expand();
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'expand', 'shouldUseCustomClose', false
        ]);
      });
    });

    describe('when called in a bad state', function() {
      beforeEach(function() {
        spyOn(MRAID, 'getState').andReturn('foo');
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.expand();
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
      });
    });

    describe('when called with default state and custom params', function() {
      beforeEach(function() {
        MRAID.setExpandProperties({
          useCustomClose: true
        });
        spyOn(MRAID, 'getState').andReturn('default');
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.expand();
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'expand', 'shouldUseCustomClose', true
        ]);
      });
    });

    describe('when called with default state and url', function() {
      beforeEach(function() {
        MRAID.setExpandProperties({
          useCustomClose: false
        });
        spyOn(MRAID, 'getState').andReturn('default');
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.expand('http://url.com');
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'expand', 'shouldUseCustomClose', false, 'url', 'http://url.com'
        ]);
      });
    });

    describe('when a user tries to set read only properties', function() {
      beforeEach(function() {
        MRAID.setExpandProperties({
          width: 150,
          height: 150,
          isModal: false
        });
      });
        
      it("does not change", function() {
        expect(MRAID.getExpandProperties()).toEqual({
          width: false,
          height: false,
          isModal: true,
          useCustomClose: false
        });
      });
    });
  });

  describe('.resize', function() {
    describe('when called in default state', function() {
      var errorSpy, stateSpy;

      beforeEach(function() {
        stateSpy = spyOn(MRAID, 'getState').andReturn('default');
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
        MRAID.resize();
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('applies args to bridge correctly', function() {
        expect(errorSpy).toHaveBeenCalled();
      });
  
      describe('when not called from a default state', function() {
        beforeEach(function() {
          stateSpy.andReturn('expanded');
        });

        it('applies args to bridge correctly', function() {
          expect(errorSpy).toHaveBeenCalled();
        });
      });
    });
    describe('when called with resized state and custom params', function() {
      beforeEach(function() {
        MRAID.setResizeProperties({
          height: 300,
          width: 500,
          offsetX: 10,
          offsetY: -10,
          customClosePosition: 'bottom-left',
          allowOffscreen: true,
        });
        spyOn(MRAID, 'getState').andReturn('resized');
        spyOn(BRIDGE, 'executeNativeCall');
        MRAID.resize();
      });

      it('applies args to bridge correctly', function() {
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'resize', 'width', 500, 'height', 300, 'offsetX', 10, 'offsetY', -10, 'customClosePosition', 'bottom-left', 'allowOffscreen', true
        ]);
      });
    });
  });

  describe('.createCalendarEvent', function() {
    describe('when called incorrectly', function() {
      var errorSpy;

      beforeEach(function() {
        spyOn(BRIDGE, 'executeNativeCall');
        errorSpy = jasmine.createSpy();
        MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      afterEach(function() {
        MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      });

      it('does not allow a calendar event with a null property dictionary', function() {
        MRAID.createCalendarEvent(null);
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      //it('calendar event with invalid start and end time values', function() {
      //  MRAID.createCalendarEvent({description:'bad day', start: 'totally incorrect', end: '20001 space time'});
      //  expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
      //  expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      //});

      //it('should not allow a calendar event with an entirely bogus reminder field', function() {
      //  MRAID.createCalendarEvent({reminder: 'entirely_bogus'});
      //  expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
      //  expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      //});

      //it('should not allow a calendar event with a relative reminder whose date is after the event has started', function() {
      //  MRAID.createCalendarEvent({reminder: '6000000'}); // Attempted reminder for 10m after start
      //  expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
      //  expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      //});

      it('does not allow a calendar event with an invalid recurrence interval', function() {
        MRAID.createCalendarEvent({recurrence: {interval: null}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with an invalid recurrence frequency', function() {
        MRAID.createCalendarEvent({recurrence: {frequency: 'aBsuRd value!'}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with a null recurrence days of the week', function() {
        MRAID.createCalendarEvent({recurrence: {daysInWeek: null}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with a null recurrence days of the month', function() {
        MRAID.createCalendarEvent({recurrence: {daysInMonth: null}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with a null recurrence days of the year', function() {
        MRAID.createCalendarEvent({recurrence: {daysInYear: null}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with a null recurrence months of the year', function() {
        MRAID.createCalendarEvent({recurrence: {monthsInYear: null}});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });

      it('should not allow a calendar event with an invalid transparency setting', function() {
        MRAID.createCalendarEvent({transparency: 'bogus'});
        expect(BRIDGE.executeNativeCall).not.toHaveBeenCalled();
        expect(errorSpy).toHaveBeenCalledWith(jasmine.any(String), 'createCalendarEvent');
      });
    });

    describe('when called correctly', function() {
      beforeEach(function() {
        spyOn(BRIDGE, 'executeNativeCall');
      });

      it('allows a calendar event with an empty property dictionary', function() {
        MRAID.createCalendarEvent({});
        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith(['createCalendarEvent']);
      });

      it('allows a calendar event with basic parameters (description, start, end, location, summary)', function() {
        MRAID.createCalendarEvent({
          description: 'Mayan Apocalypse/End of World',
          start: '2113-07-19T20:00:00-04:00',
          end: '2113-07-19T21:00:00-04:00',
          location: 'Tikal, Guatemala',
          summary: 'You are going to have a bad time'
        });

        expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
          'createCalendarEvent',
          'description', 'Mayan Apocalypse/End of World',
          'location', 'Tikal, Guatemala',
          'summary', 'You are going to have a bad time',
          'start', '2113-07-19T20:00:00-04:00',
          'end', '2113-07-19T21:00:00-04:00'
        ]);
      });

      describe('events with reminders', function() {
        it('allows a calendar event with an absolute reminder', function() {
          MRAID.createCalendarEvent({reminder: '2113-07-19T19:50:00-04:00'}); // 10m before start
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'absoluteReminder', '2113-07-19T19:50:00-04:00'
          ]);
        });

        it('allows a calendar event with an negative relative reminder', function() {
          MRAID.createCalendarEvent({reminder: '-600000'}); // 10m before start
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'relativeReminder', -600
          ]);
        });
      });

      describe('recurring events', function() {
        it('should allow a calendar event with a valid recurrence interval', function() {
          MRAID.createCalendarEvent({
            recurrence: {interval: 2}
          });
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 2
          ]);
        });

        it('should use a default value of 1 for recurrence interval when no interval is sent', function() {
          MRAID.createCalendarEvent({
            recurrence: {}
          });
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 1
          ]);
        });

        it('should allow a calendar event with a valid recurrence frequency', function() {
          var validFrequencies = ['daily', 'weekly', 'monthly', 'yearly'];
          for (var i = 0; i < validFrequencies.length; i++) {
            var currentFrequency = validFrequencies[i];
            MRAID.createCalendarEvent({recurrence: {frequency: currentFrequency}});
            expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
              'createCalendarEvent',
              'interval', 1,
              'frequency', currentFrequency
            ]);
          }
        });

        it('should allow a calendar event that repeats up until a certain recurrence end date', function() {
          MRAID.createCalendarEvent({
            recurrence: {frequency: 'weekly', interval: 2, expires: '2114-07-19T20:00:00-04:00'} // recurrence ends 1 year later
          });
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 2,
            'frequency', 'weekly',
            'expires', '2114-07-19T20:00:00-04:00'
          ]);
        });

        it('should allow a calendar event that repeats forever', function() {
          MRAID.createCalendarEvent({
            recurrence: {frequency: 'weekly', interval: 2, expires: null}
          });
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 2,
            'frequency', 'weekly'
          ]);
        });

        it('should allow a calendar event that repeats for given days of the week', function() {
          MRAID.createCalendarEvent({recurrence: {daysInWeek: [1,2]}});
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 1,
            'daysInWeek', '1,2'
          ]);
        });

        it('should allow a calendar event that repeats for given days of the month', function() {
          MRAID.createCalendarEvent({recurrence: {daysInMonth: [1,2]}});
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 1,
            'daysInMonth', '1,2'
          ]);
        });

        it('should allow a calendar event that repeats for given days of the year', function() {
          MRAID.createCalendarEvent({recurrence: {daysInYear: [1,2]}});
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 1,
            'daysInYear', '1,2'
          ]);
        });

        it('should allow a calendar event that repeats for given months of the year', function() {
          MRAID.createCalendarEvent({recurrence: {monthsInYear: [1,2]}});
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'interval', 1,
            'monthsInYear', '1,2'
          ]);
        });
      });

      describe('events with transparency', function() {
        it('should allow a calendar event to mark the participant as busy during the event', function() {
          MRAID.createCalendarEvent({transparency: 'opaque'}); // 'opaque' means busy
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'transparency', 'opaque'
          ]);
        });

        it('should allow a calendar event to mark the participant as free during the event', function() {
          MRAID.createCalendarEvent({transparency: 'transparent'}); // 'transparent' means free
          expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
            'createCalendarEvent',
            'transparency', 'transparent'
          ]);
        });
      });
    });
  });

  describe("bridge accessors", function() {
    it("should be possible to directly set current position", function() {
      BRIDGE.setCurrentPosition(10, 10, 20, 20);
      expect(MRAID.getCurrentPosition()).toEqual({
        x: 10,
        y: 10,
        width: 20,
        height: 20
      });
    });

    it("should be possible to directly set default position", function() {
      BRIDGE.setDefaultPosition(10, 10, 20, 20);
      expect(MRAID.getDefaultPosition()).toEqual({
        x: 10,
        y: 10,
        width: 20,
        height: 20
      });
    });

    it("should be possible to set max size", function() {
      BRIDGE.setMaxSize(100, 100);
      expect(MRAID.getMaxSize()).toEqual({
        width: 100,
        height: 100
      });
      expect(MRAID.getExpandProperties()).toEqual({
        width: 100,
        height: 100,
        useCustomClose: false,
        isModal: true
      });
    });

    it("should be possible to set placement type", function() {
      BRIDGE.setPlacementType(MRAID.PLACEMENT_TYPES.INLINE);
      expect(MRAID.getPlacementType()).toEqual(MRAID.PLACEMENT_TYPES.INLINE);
    });

    it("should be possible to set screen size", function() {
      BRIDGE.setScreenSize(150, 150);
      expect(MRAID.getScreenSize()).toEqual({
        width: 150,
        height: 150
      });
    });

    it("should be possible to set the state", function() {
      BRIDGE.setState(MRAID.STATES.EXPANDED);
      expect(MRAID.getState()).toEqual(MRAID.STATES.EXPANDED);

      BRIDGE.setState(MRAID.STATES.RESIZED);
      expect(MRAID.getState()).toEqual(MRAID.STATES.RESIZED);
    });

    it("should be possible to set the isViewable", function() {
      BRIDGE.setIsViewable(true);
      expect(MRAID.isViewable()).toEqual(true);

      BRIDGE.setIsViewable(false);
      expect(MRAID.isViewable()).toEqual(false);
    });

    it("should be possible to set supports", function() {
      expect(MRAID.supports('sms')).toEqual(false);

      BRIDGE.setSupports(true, true, true, true, true);
      expect(MRAID.supports('sms')).toEqual(true);
    });
  });

  describe("bridge notifiers", function() {
    var sizeChangeSpy, readySpy, errorSpy, stateSpy, viewableChangeSpy;
    beforeEach(function() {
      sizeChangeSpy = jasmine.createSpy();
      readySpy = jasmine.createSpy();
      errorSpy = jasmine.createSpy();
      stateSpy = jasmine.createSpy();
      viewableChangeSpy = jasmine.createSpy();

      MRAID.addEventListener(MRAID.EVENTS.SIZECHANGE, sizeChangeSpy);
      MRAID.addEventListener(MRAID.EVENTS.READY, readySpy);
      MRAID.addEventListener(MRAID.EVENTS.ERROR, errorSpy);
      MRAID.addEventListener(MRAID.EVENTS.STATECHANGE, stateSpy);
      MRAID.addEventListener(MRAID.EVENTS.VIEWABLECHANGE, viewableChangeSpy);
    });

    afterEach(function() {
      MRAID.removeEventListener(MRAID.EVENTS.SIZECHANGE, sizeChangeSpy);
      MRAID.removeEventListener(MRAID.EVENTS.READY, readySpy);
      MRAID.removeEventListener(MRAID.EVENTS.ERROR, errorSpy);
      MRAID.removeEventListener(MRAID.EVENTS.STATECHANGE, stateSpy);
      MRAID.removeEventListener(MRAID.EVENTS.VIEWABLECHANGE, viewableChangeSpy);
    });

    describe("When it is first called", function() {
      beforeEach(function() {
        BRIDGE.notifySizeChangeEvent(20, 10);
      });
        
      it("should broadcast width and height on first notify", function() {
        expect(sizeChangeSpy).toHaveBeenCalledWith(20, 10);
      });

      describe("On subsequent calls", function() {
        beforeEach(function() {
          sizeChangeSpy.reset();
        });
          
        it("should only broadcast once", function() {
          BRIDGE.notifySizeChangeEvent(20, 10);
          expect(sizeChangeSpy).not.toHaveBeenCalled();
        });

        it("should broadcast when changed", function() {          
          BRIDGE.notifySizeChangeEvent(10, 20);
          expect(sizeChangeSpy).toHaveBeenCalledWith(10, 20);
        });
      });
    });

    describe("notify ready event", function() {
      it("calls the ready spy", function() {
        BRIDGE.notifyReadyEvent();
        expect(readySpy).toHaveBeenCalled();
      });
    });

    describe("notify error event", function() {
      it("calls the error spy", function() {
        BRIDGE.notifyErrorEvent();
        expect(errorSpy).toHaveBeenCalled();
      });
    });

    describe("notify state event", function() {
      it("it calls the statechange spy", function() {
        BRIDGE.notifyStateChangeEvent();
        expect(stateSpy).toHaveBeenCalled();
      });
    });

    describe("notify viewable change event", function() {
      it("calls the viewable change spy", function() {
        BRIDGE.notifyViewableChangeEvent();
        expect(viewableChangeSpy).toHaveBeenCalled();
      });
    });
  });

  describe("orientationProperties", function() {
    beforeEach(function() {
      spyOn(BRIDGE, 'executeNativeCall');
    });

    it("can be set and get", function() {
      var props;

      MRAID.setOrientationProperties({allowOrientationChange: true, forceOrientation: 'portrait'});
      props = MRAID.getOrientationProperties();
      expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
        'setOrientationProperties', 'allowOrientationChange', true, 'forceOrientation', 'portrait'
      ]);
      expect(props).toEqual({allowOrientationChange: true, forceOrientation: 'portrait'});

      BRIDGE.executeNativeCall.reset();

      MRAID.setOrientationProperties({allowOrientationChange: false, forceOrientation: 'landscape'});
      props = MRAID.getOrientationProperties();
      expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
        'setOrientationProperties', 'allowOrientationChange', false, 'forceOrientation', 'landscape'
      ]);
      expect(props).toEqual({allowOrientationChange: false, forceOrientation: 'landscape'});
    });

    it("can set one but not both properties", function() {
      var props;

      MRAID.setOrientationProperties({allowOrientationChange: true, forceOrientation: 'portrait'});
      props = MRAID.getOrientationProperties();
      expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
        'setOrientationProperties', 'allowOrientationChange', true, 'forceOrientation', 'portrait'
      ]);
      expect(props).toEqual({allowOrientationChange: true, forceOrientation: 'portrait'});

      BRIDGE.executeNativeCall.reset();

      MRAID.setOrientationProperties({allowOrientationChange: false});
      props = MRAID.getOrientationProperties();
      expect(BRIDGE.executeNativeCall).toHaveBeenCalledWith([
        'setOrientationProperties', 'allowOrientationChange', false, 'forceOrientation', 'portrait'
      ]);
      expect(props).toEqual({allowOrientationChange: false, forceOrientation: 'portrait'});
    });
  });

  describe("host SDK version", function() {
    afterEach(function() {
      BRIDGE.fireChangeEvent({hostSDKVersion: '0.0.0'});
    });

    it("should default to 0.0.0", function() {
      var version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(0);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(0);
    });

    it("should be possible to change the value using a properly-formatted version string", function() {
      var version = MRAID.getHostSDKVersion();

      BRIDGE.fireChangeEvent({hostSDKVersion: '5.0.4'});
      expect(version['major']).toEqual(5);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(4);

      BRIDGE.fireChangeEvent({hostSDKVersion: '1.2.3+4.5'});
      version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(1);
      expect(version['minor']).toEqual(2);
      expect(version['patch']).toEqual(3);
    });

    it("should not be possible to change the value using a bad version string", function() {
      var version;

      BRIDGE.fireChangeEvent({hostSDKVersion: ''});
      version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(0);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(0);

      BRIDGE.fireChangeEvent({hostSDKVersion: '1.5'});
      version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(0);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(0);

      BRIDGE.fireChangeEvent({hostSDKVersion: 'a.b.c'});
      version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(0);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(0);

      BRIDGE.fireChangeEvent({hostSDKVersion: '-1.5.5'});
      version = MRAID.getHostSDKVersion();
      expect(version['major']).toEqual(0);
      expect(version['minor']).toEqual(0);
      expect(version['patch']).toEqual(0);
    });
  });
});
