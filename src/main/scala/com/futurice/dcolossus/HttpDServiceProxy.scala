package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.protocols.http.Http
import colossus.service.ServiceConfig
import fi.veikkaus.dcontext.MutableDContext

class DHttpServiceProxy(config:ServiceConfig,
                        serverContext:ServerContext,
                        service:DService[Http])
  extends DServiceProxy[Http](config, serverContext, service) {}

